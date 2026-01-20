package ai.anam.lab.fakes

import ai.anam.lab.Message
import ai.anam.lab.webrtc.MessagingClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A fake implementation of [MessagingClient] designed for unit tests. Allows tests to manually emit messages to the flow
 * for testing purposes.
 */
internal class FakeMessagingClient : MessagingClient {
    private val _messages = Channel<List<Message>>(Channel.BUFFERED)
    override val messages: Flow<List<Message>> = _messages.receiveAsFlow()

    /**
     * Emit a list of [Message]s to the messages flow. This allows tests to simulate messages being received.
     */
    suspend fun emitMessages(messages: List<Message>) {
        _messages.send(messages)
    }
}
