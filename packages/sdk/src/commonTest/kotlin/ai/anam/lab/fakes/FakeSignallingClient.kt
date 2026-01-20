package ai.anam.lab.fakes

import ai.anam.lab.SessionEvent
import ai.anam.lab.api.SignalMessage
import ai.anam.lab.webrtc.SignallingClient
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A fake implementation of [SignallingClient] designed for unit tests. Allows tests to manually emit values to the flows
 * and track method calls for testing purposes.
 */
internal class FakeSignallingClient : SignallingClient {
    private val _connected = MutableStateFlow(false)
    override val connected: Flow<Boolean> = _connected.asStateFlow()

    private val _events = MutableSharedFlow<SessionEvent>()
    override val events: Flow<SessionEvent> = _events.asSharedFlow()

    private val _received = MutableSharedFlow<SignalMessage>()
    override val received: Flow<SignalMessage> = _received.asSharedFlow()

    // Track method calls for testing purposes
    val sentOffers = mutableListOf<SessionDescription>()
    val sentIceCandidates = mutableListOf<IceCandidate>()

    /**
     * Emit a [SessionEvent] to the events flow. This allows tests to simulate events being emitted by the signalling
     * client.
     */
    suspend fun emitEvent(event: SessionEvent) {
        _events.emit(event)
    }

    /**
     * Emit a [SignalMessage] to the received flow. This allows tests to simulate signal messages being received.
     */
    suspend fun emitReceived(message: SignalMessage) {
        _received.emit(message)
    }

    override suspend fun connect() {
        try {
            _connected.value = true
            awaitCancellation()
        } finally {
            _connected.value = false
        }
    }

    override suspend fun sendOffer(localDescription: SessionDescription) {
        sentOffers.add(localDescription)
    }

    override suspend fun sendIceCandidate(candidate: IceCandidate) {
        sentIceCandidates.add(candidate)
    }

    override suspend fun sendTalkMessage(
        content: String,
        startOfSpeech: Boolean,
        endOfSpeech: Boolean,
        correlationId: String,
    ) {
        // No-Op (for now).
    }
}
