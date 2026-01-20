package ai.anam.lab.fakes

import ai.anam.lab.SessionEvent
import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.UserDataMessage
import ai.anam.lab.webrtc.StreamingClient
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of [StreamingClient] designed for unit tests. Allows tests to manually emit values to the flows
 * for testing purposes.
 */
internal class FakeStreamingClient : StreamingClient {
    private val _connected = MutableStateFlow(false)
    val connected: Flow<Boolean> = _connected.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>()
    override val events: Flow<SessionEvent> = _events.asSharedFlow()

    private val _dataChannelMessages = MutableSharedFlow<DataChannelMessage>()
    override val dataChannelMessages: Flow<DataChannelMessage> = _dataChannelMessages.asSharedFlow()

    /**
     * Tracks all messages sent via [sendDataMessage] for test verification.
     */
    val sentMessages = mutableListOf<UserDataMessage>()

    /**
     * Emit a [SessionEvent] to the events flow. This allows tests to simulate events being emitted by the streaming
     * client.
     */
    suspend fun emitEvent(event: SessionEvent) {
        _events.emit(event)
    }

    /**
     * Emit a [DataChannelMessage] to the dataChannelMessages flow. This allows tests to simulate data channel messages
     * being received.
     */
    suspend fun emitDataChannelMessage(message: DataChannelMessage) {
        _dataChannelMessages.emit(message)
    }

    override suspend fun connect() {
        try {
            _connected.value = true
            awaitCancellation()
        } finally {
            _connected.value = false
        }
    }

    override fun sendDataMessage(message: UserDataMessage) {
        sentMessages.add(message)
    }
}
