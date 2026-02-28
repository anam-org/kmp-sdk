package ai.anam.lab.webrtc

import ai.anam.lab.SessionEvent
import ai.anam.lab.ToolCallCompletedPayload
import ai.anam.lab.ToolCallFailedPayload
import ai.anam.lab.ToolCallHandler
import ai.anam.lab.ToolCallStartedPayload
import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.cancellableRunCatching
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal interface ToolCallClient {
    /**
     * A flow of [SessionEvent]s related to tool calls (Started, Completed, Failed).
     */
    val events: Flow<SessionEvent>

    /**
     * Registers a [ToolCallHandler] for the given [toolName]. Returns a function that, when called,
     * unregisters the handler.
     */
    fun registerHandler(toolName: String, handler: ToolCallHandler): () -> Unit

    /**
     * Releases all resources held by this client, including pending call tracking and registered handlers.
     * Called when the associated [Session] closes.
     */
    fun release()

    /**
     * Starts processing tool call messages from the data channel. This is a suspending function that collects
     * incoming messages and processes them for the lifetime of the coroutine scope.
     */
    suspend fun processMessages()
}

@OptIn(ExperimentalAtomicApi::class)
internal class ToolCallClientImpl(
    private val dataChannelMessages: Flow<DataChannelMessage>,
    private val logger: Logger,
    private val clock: Clock = Clock.System,
) : ToolCallClient {

    // Map of toolCallId -> start timestamp in millis (for execution time calculation).
    // Uses AtomicReference with copy-on-write for safe access from both the processing coroutine
    // and release() (which may run concurrently during cancellation teardown).
    private val pendingCalls = AtomicReference(emptyMap<String, Long>())

    // Map of toolName -> ToolCallHandler. Uses AtomicReference with copy-on-write to allow
    // thread-safe registration from any thread while the processing coroutine reads handlers.
    private val handlers = AtomicReference(emptyMap<String, ToolCallHandler>())

    private val _events = MutableSharedFlow<SessionEvent>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val events: Flow<SessionEvent> = _events.asSharedFlow()

    override fun registerHandler(toolName: String, handler: ToolCallHandler): () -> Unit {
        handlers.update { it + (toolName to handler) }
        return { handlers.update { it - toolName } }
    }

    override fun release() {
        pendingCalls.store(emptyMap())
        handlers.store(emptyMap())
    }

    override suspend fun processMessages() {
        dataChannelMessages.collect { message ->
            val payload = message.data as? DataChannelMessagePayload.ToolCallMessage ?: return@collect
            processMessage(payload)
        }
    }

    private suspend fun processMessage(message: DataChannelMessagePayload.ToolCallMessage) {
        when (message) {
            is DataChannelMessagePayload.ToolCallStartedMessage -> processStarted(message)
            is DataChannelMessagePayload.ToolCallCompletedMessage -> processCompleted(message)
            is DataChannelMessagePayload.ToolCallFailedMessage -> processFailed(message)
        }
    }

    private suspend fun processStarted(message: DataChannelMessagePayload.ToolCallStartedMessage) {
        val startTime = clock.now().toEpochMilliseconds()
        pendingCalls.update { it + (message.toolCallId to startTime) }

        val publicPayload = message.toPayload()
        _events.tryEmit(SessionEvent.ToolCallStarted(publicPayload))

        val handler = handlers.load()[message.toolName] ?: return

        if (message.toolType == CLIENT_TOOL_TYPE) {
            val onStart = handler.onStart
            if (onStart != null) {
                // For client tools, invoke the handler and auto-complete/fail based on the result.
                cancellableRunCatching {
                    onStart.invoke(publicPayload)
                }.onSuccess { result ->
                    val completedPayload = createCompletedPayload(publicPayload, startTime, result)
                    _events.tryEmit(SessionEvent.ToolCallCompleted(completedPayload))
                    cancellableRunCatching { handler.onComplete?.invoke(completedPayload) }
                        .onFailure { e ->
                            logger.e(TAG, e) { "Error in onComplete handler for tool ${message.toolName}" }
                        }
                }.onFailure { e ->
                    logger.e(TAG, e) { "Error in onStart handler for tool ${message.toolName}" }
                    val failedPayload = createFailedPayload(publicPayload, startTime, e.message ?: "Unknown error")
                    _events.tryEmit(SessionEvent.ToolCallFailed(failedPayload))
                    cancellableRunCatching { handler.onFail?.invoke(failedPayload) }
                        .onFailure { e2 ->
                            logger.e(TAG, e2) { "Error in onFail handler for tool ${message.toolName}" }
                        }
                }
            }

            pendingCalls.update { it - message.toolCallId }
        } else {
            // For server tools, just invoke onStart (server will send completed/failed later).
            cancellableRunCatching { handler.onStart?.invoke(publicPayload) }
                .onFailure { e -> logger.e(TAG, e) { "Error in onStart handler for tool ${message.toolName}" } }
        }
    }

    private suspend fun processCompleted(message: DataChannelMessagePayload.ToolCallCompletedMessage) {
        val startTime = pendingCalls.load()[message.toolCallId]
        pendingCalls.update { it - message.toolCallId }
        val executionTime = startTime?.let { clock.now().toEpochMilliseconds() - it }

        val publicPayload = message.toPayload(executionTime)
        _events.tryEmit(SessionEvent.ToolCallCompleted(publicPayload))

        val handler = handlers.load()[message.toolName] ?: return
        cancellableRunCatching { handler.onComplete?.invoke(publicPayload) }
            .onFailure { e -> logger.e(TAG, e) { "Error in onComplete handler for tool ${message.toolName}" } }
    }

    private suspend fun processFailed(message: DataChannelMessagePayload.ToolCallFailedMessage) {
        val startTime = pendingCalls.load()[message.toolCallId]
        pendingCalls.update { it - message.toolCallId }
        val executionTime = startTime?.let { clock.now().toEpochMilliseconds() - it }

        val publicPayload = message.toPayload(executionTime)
        _events.tryEmit(SessionEvent.ToolCallFailed(publicPayload))

        val handler = handlers.load()[message.toolName] ?: return
        cancellableRunCatching { handler.onFail?.invoke(publicPayload) }
            .onFailure { e -> logger.e(TAG, e) { "Error in onFail handler for tool ${message.toolName}" } }
    }

    private fun createCompletedPayload(
        started: ToolCallStartedPayload,
        startTime: Long,
        result: String?,
    ): ToolCallCompletedPayload {
        val now = clock.now()
        return ToolCallCompletedPayload(
            eventUid = started.eventUid,
            toolCallId = started.toolCallId,
            toolName = started.toolName,
            toolType = started.toolType,
            toolSubtype = started.toolSubtype,
            arguments = started.arguments,
            result = result,
            executionTime = now.toEpochMilliseconds() - startTime,
            timestamp = now.toString(),
            timestampUserAction = started.timestampUserAction,
            userActionCorrelationId = started.userActionCorrelationId,
            documentsAccessed = null,
        )
    }

    private fun createFailedPayload(
        started: ToolCallStartedPayload,
        startTime: Long,
        errorMessage: String,
    ): ToolCallFailedPayload {
        val now = clock.now()
        return ToolCallFailedPayload(
            eventUid = started.eventUid,
            toolCallId = started.toolCallId,
            toolName = started.toolName,
            toolType = started.toolType,
            toolSubtype = started.toolSubtype,
            arguments = started.arguments,
            errorMessage = errorMessage,
            executionTime = now.toEpochMilliseconds() - startTime,
            timestamp = now.toString(),
            timestampUserAction = started.timestampUserAction,
            userActionCorrelationId = started.userActionCorrelationId,
        )
    }

    private companion object {
        const val TAG = "ToolCallClient"
        const val CLIENT_TOOL_TYPE = "client"
    }
}

/**
 * Atomically updates the value of this [AtomicReference] using a compare-and-swap loop.
 */
@ExperimentalAtomicApi
private inline fun <T> AtomicReference<T>.update(transform: (T) -> T) {
    while (true) {
        val current = load()
        val updated = transform(current)
        if (compareAndSet(current, updated)) return
    }
}
