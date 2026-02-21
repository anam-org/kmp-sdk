package ai.anam.lab.fakes

import ai.anam.lab.ReasoningMessage
import ai.anam.lab.webrtc.ReasoningClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A fake implementation of [ReasoningClient] designed for unit tests. Allows tests to manually emit reasoning messages
 * to the flow for testing purposes.
 */
internal class FakeReasoningClient : ReasoningClient {
    private val _reasoningMessages = Channel<List<ReasoningMessage>>(Channel.BUFFERED)
    override val reasoningMessages: Flow<List<ReasoningMessage>> = _reasoningMessages.receiveAsFlow()

    /**
     * Emit a list of [ReasoningMessage]s to the reasoningMessages flow. This allows tests to simulate reasoning
     * messages being received.
     */
    suspend fun emitReasoningMessages(messages: List<ReasoningMessage>) {
        _reasoningMessages.send(messages)
    }
}
