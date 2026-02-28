package ai.anam.lab.fakes

import ai.anam.lab.SessionEvent
import ai.anam.lab.ToolCallHandler
import ai.anam.lab.webrtc.ToolCallClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A fake implementation of [ToolCallClient] designed for unit tests.
 */
internal class FakeToolCallClient : ToolCallClient {
    private val _events = MutableSharedFlow<SessionEvent>()
    override val events: Flow<SessionEvent> = _events.asSharedFlow()

    val registeredHandlers = mutableMapOf<String, ToolCallHandler>()
    var released = false
        private set

    suspend fun emitEvent(event: SessionEvent) {
        _events.emit(event)
    }

    override fun registerHandler(toolName: String, handler: ToolCallHandler): () -> Unit {
        registeredHandlers[toolName] = handler
        return { registeredHandlers.remove(toolName) }
    }

    override fun release() {
        released = true
    }

    override suspend fun processMessages() {
        // No-op in tests - events are emitted manually via emitEvent.
    }
}
