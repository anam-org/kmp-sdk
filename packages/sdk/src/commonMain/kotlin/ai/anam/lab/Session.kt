package ai.anam.lab

import ai.anam.lab.api.UserDataMessage
import ai.anam.lab.utils.Logger
import ai.anam.lab.webrtc.MediaStreamManager
import ai.anam.lab.webrtc.MessagingClient
import ai.anam.lab.webrtc.SignallingClient
import ai.anam.lab.webrtc.StreamingClient
import ai.anam.lab.webrtc.ToolClient
import com.shepeliev.webrtckmp.AudioTrack
import com.shepeliev.webrtckmp.VideoTrack
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch

/**
 * Internal data class that represents the two remote Tracks from the Persona.
 */
internal data class SessionTracks(val audioTrack: AudioTrack, val videoTrack: VideoTrack)

/**
 * This class represents an active session with the Anam SDK. The call to [start] will begin the session, and its
 * lifetime is linked to the CoroutineScope of the call. When the scope is cancelled, the Session will be cleaned up
 * along with any obtained resources.
 *
 * ## Lifecycle
 *
 * A [Session] transitions through the following states:
 * 1. **Created** — constructed via [AnamClient], [isActive] is `false`.
 * 2. **Active** — after [start] is called, [isActive] becomes `true` and the session connects its signalling,
 *    streaming, and platform clients. [events] begins emitting [SessionEvent]s.
 * 3. **Closed** — when a [SessionEvent.ConnectionClosed] event is received or the parent coroutine scope is cancelled,
 *    all jobs are cancelled, resources are released, and [isActive] returns to `false`.
 *
 * Calling [start] more than once on the same [Session] instance is not supported and will result in duplicate
 * connection attempts.
 *
 * [isActive] is thread-safe and may be read from any thread.
 */
@OptIn(ExperimentalAtomicApi::class)
public class Session internal constructor(
    public val id: String,
    private val signallingClient: SignallingClient,
    private val streamingClient: StreamingClient,
    private val mediaStreamManager: MediaStreamManager,
    private val messagingClient: MessagingClient,
    private val toolClient: ToolClient,
    private val sessionManager: PlatformSessionManager,
    private val logger: Logger,
    private val isLoggingEnabled: Boolean = true,
    private val clock: Clock = Clock.System,
) {
    private val _isActive = AtomicBoolean(false)

    /**
     * Whether the session is actively streaming. This property is thread-safe and may be read from any thread.
     */
    public val isActive: Boolean
        get() = _isActive.load()

    /**
     * Whether the local audio stream (microphone) is currently muted.
     */
    public val isLocalAudioMuted: Boolean
        get() = mediaStreamManager.isLocalAudioMuted

    /**
     * Mutes or unmutes the local audio stream (microphone). This can be called before the session has started;
     * the mute state will be applied once the audio stream is initialized.
     *
     * @param muted `true` to mute the microphone, `false` to unmute.
     */
    public fun setLocalAudioMuted(muted: Boolean) {
        mediaStreamManager.isLocalAudioMuted = muted
    }

    /**
     * Access to the AudioTrack and VideoTrack that the Persona will be rendered using. This is internal, to allow it to
     * be accessed by the [AnamVideo] component. The [Session] is provided to the [AnamVideo] Composable, which will be
     * able to access the video track to render to the local Surface.
     */
    internal val tracks = combine(
        mediaStreamManager.remoteAudioTrack.filterNotNull(),
        mediaStreamManager.remoteVideoTrack.filterNotNull(),
    ) { (audio, video) ->
        SessionTracks(audio as AudioTrack, video as VideoTrack)
    }

    // A local flow of events that the session can emit itself (e.g. first video frame rendered).
    // Uses DROP_OLDEST to ensure the flow never blocks emitters for non-critical local events.
    private val localEvents = MutableSharedFlow<SessionEvent>(
        extraBufferCapacity = 5,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * The [SessionEvent]s associated with this [Session].
     */
    public val events: Flow<SessionEvent> = merge(
        signallingClient.events,
        streamingClient.events,
        localEvents.asSharedFlow(),
    ).transformWhile { event ->
        // When we receive a ConnectionClosed event, it indicates that it's terminal. We should not emit any further
        // events as we tear down our connection(s). This avoids any duplicated events with different "reasons" (or
        // codes)
        emit(event)
        event !is SessionEvent.ConnectionClosed
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())

    /**
     * The [Message]s associated with this [Session].
     */
    public val messages: Flow<List<Message>> = _messages.asStateFlow()

    private val _toolEvents = MutableSharedFlow<ToolEvent>(extraBufferCapacity = 1)

    /**
     * The [ToolEvent]s associated with this [Session]. These represent tool calls requested by the Persona.
     */
    public val toolEvents: Flow<ToolEvent> = _toolEvents.asSharedFlow()

    // Internally, we track all the jobs we have scheduled. This will allow us to cancel them as soon as we detect an
    // error with any one of our required infrastructure.
    private val jobs = mutableListOf<Job>()

    public suspend fun start(): Unit = coroutineScope {
        logger.i(TAG) { "Starting session..." }
        localEvents.emit(SessionEvent.Connecting)

        _isActive.store(true)
        try {
            // Since we're starting the Session, let's connect to our various Clients as well as configure any platform
            // specific management.
            jobs += listOf(
                signallingClient::connect,
                streamingClient::connect,

                // The platform specific session manager is responsible for dealing with any local resources that need
                // to be managed during the lifetime of the session. For example, any network lock or audio focus
                // requests.
                sessionManager::start,
            ).map { action ->
                launch { action() }
            }

            // Collect any incoming messages from the MessagingClient so that we can expose them to the consumer. If we
            // just provided direct access to the flow from the MessagingClient, then these messages would only be
            // available if collected. This way, we tied the set of messages to the scope of this Session.
            jobs += launch {
                messagingClient.messages.collect { messages ->
                    _messages.value = messages
                }
            }

            // Collect tool events from the ToolClient and forward them to both the dedicated tool events flow and the
            // unified session events flow.
            jobs += launch {
                toolClient.toolEvents.collect { event ->
                    _toolEvents.emit(event)
                    localEvents.emit(SessionEvent.ToolCall(event))
                }
            }

            if (isLoggingEnabled) {
                jobs += launch {
                    messages.collect { message ->
                        logger.i(TAG) { "Message History: $message" }
                    }
                }

                jobs += launch {
                    toolEvents.collect { event ->
                        logger.i(TAG) { "Tool Event: $event" }
                    }
                }

                jobs += launch {
                    events.collect { event ->
                        logger.i(TAG) { "Session Event: $event" }
                    }
                }
            }

            awaitConnectionClosed()
        } finally {
            _isActive.store(false)

            // If we've detected that our connection has been closed, we need to manually cancel all of our jobs. This
            // will prevent us from keeping one connection open while the other(s) have failed. In the event of our
            // main coroutine context being cancelled, these will effectively be a no-op.
            jobs.forEach { job -> job.cancel() }
            jobs.clear()

            // Release any additional resources.
            mediaStreamManager.release()
        }
    }

    /**
     * Sends a user text message in the active streaming session.
     *
     * @return `true` if the message was sent successfully, `false` if the session is not active or the send failed.
     */
    public fun sendUserMessage(content: String): Boolean {
        if (!isActive) return false

        return streamingClient.sendDataMessage(
            UserDataMessage.UserTextMessage(
                content = content,
                sessionId = id,
                timestamp = getCurrentTimestamp(),
            ),
        )
    }

    /**
     * Interrupts the Persona in the active streaming session.
     *
     * @return `true` if the interrupt was sent successfully, `false` if the session is not active or the send failed.
     */
    public fun interruptPersona(): Boolean {
        if (!isActive) return false

        return streamingClient.sendDataMessage(
            UserDataMessage.PersonaInterruptMessage(
                sessionId = id,
                timestamp = getCurrentTimestamp(),
            ),
        )
    }

    internal fun onFirstFrameRendered() {
        // The attached VideoSession has notified us that it's been able to render its first frame. Let's emit this
        // so that any consumer can also detect it.
        localEvents.tryEmit(SessionEvent.VideoPlayStarted)
    }

    /**
     * Helper function to wait for the first [SessionEvent.ConnectionClosed] event.
     */
    private suspend fun awaitConnectionClosed() {
        events.first { event ->
            event is SessionEvent.ConnectionClosed
        }
    }

    /**
     * Generates a timestamp suitable for [UserDataMessage]'s.
     *
     * The backend API expects ISO 8601 timestamps without the trailing "Z" UTC designator
     * (e.g. "2024-01-15T10:30:00.000"). Removing the "Z" is intentional — do not "fix" this.
     */
    private fun getCurrentTimestamp(): String = clock.now().toString().replace("Z", "")

    private companion object Companion {
        const val TAG = "Session"
    }
}
