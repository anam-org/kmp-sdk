package ai.anam.lab.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.serialization.KSerializer

class SignallingApiTest {

    // Let's use the default JSON configuration.
    private val json = defaultJsonConfiguration

    @Test
    fun `SignalMessageType serializes and deserializes correctly`() {
        testEnumSerialization(
            listOf(
                SignalMessageType.Offer,
                SignalMessageType.Answer,
                SignalMessageType.IceCandidate,
                SignalMessageType.EndSession,
                SignalMessageType.Heartbeat,
                SignalMessageType.Warning,
                SignalMessageType.TalkStreamInterrupted,
                SignalMessageType.TalkStreamInput,
                SignalMessageType.SessionReady,
            ),
            SignalMessageType.serializer(),
        )
    }

    @Test
    fun `RTCSessionDescriptionType serializes and deserializes correctly`() {
        testEnumSerialization(
            listOf(
                RTCSessionDescriptionType.Answer,
                RTCSessionDescriptionType.Offer,
                RTCSessionDescriptionType.ProvisionalAnswer,
                RTCSessionDescriptionType.Rollback,
            ),
            RTCSessionDescriptionType.serializer(),
        )
    }

    @Test
    fun `RTCIceCandidateType serializes and deserializes correctly`() {
        testEnumSerialization(
            listOf(
                RTCIceCandidateType.Host,
                RTCIceCandidateType.ServerReflexive,
                RTCIceCandidateType.PeerReflexive,
                RTCIceCandidateType.Relay,
            ),
            RTCIceCandidateType.serializer(),
        )
    }

    @Test
    fun `RTCSessionDescription serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.RTCSessionDescription(
                sdp = "v=0\r\no=- 123456789 123456789 IN IP4 127.0.0.1\r\ns=-\r\nt=0 0\r\n",
                type = RTCSessionDescriptionType.Offer,
            ),
            SignalMessagePayload.RTCSessionDescription.serializer(),
        )
    }

    @Test
    fun `Offer payload serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.Offer(
                connectionDescription = SignalMessagePayload.RTCSessionDescription(
                    sdp = "v=0\r\no=- 123456789 123456789 IN IP4 127.0.0.1\r\n",
                    type = RTCSessionDescriptionType.Offer,
                ),
                sessionId = "test-session-id",
            ),
            SignalMessagePayload.Offer.serializer(),
        )
    }

    @Test
    fun `RTCIceCandidate with all fields serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.RTCIceCandidate(
                address = "192.168.1.1",
                candidate = "candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host",
                component = "rtp",
                foundation = "1",
                port = 54321,
                protocol = "udp",
                relatedAddress = "192.168.1.2",
                relatedPort = 54322,
                sdpMid = "audio",
                sdpMLineIndex = 0,
                tcpType = "active",
                type = RTCIceCandidateType.Host,
                usernameFragment = "ufrag",
            ),
            SignalMessagePayload.RTCIceCandidate.serializer(),
        )
    }

    @Test
    fun `RTCIceCandidate with minimal fields serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.RTCIceCandidate(
                candidate = "candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host",
            ),
            SignalMessagePayload.RTCIceCandidate.serializer(),
        )
    }

    @Test
    fun `TalkMessage serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.TalkMessage(
                content = "Hello, world!",
                startOfSpeech = true,
                endOfSpeech = false,
                correlationId = "corr-123",
            ),
            SignalMessagePayload.TalkMessage.serializer(),
        )
    }

    @Test
    fun `Raw payload serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.Raw(value = "raw payload content"),
            SignalMessagePayload.Raw.serializer(),
        )
    }

    @Test
    fun `Raw payload with empty value serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.Raw(),
            SignalMessagePayload.Raw.serializer(),
        )
    }

    @Test
    fun `Unknown payload serializes and deserializes correctly`() {
        testSerialization(
            SignalMessagePayload.Empty,
            SignalMessagePayload.Empty.serializer(),
        )
    }

    @Test
    fun `SignalMessage with Offer payload serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.Offer,
                sessionId = "session-123",
                payload = SignalMessagePayload.Offer(
                    connectionDescription = SignalMessagePayload.RTCSessionDescription(
                        sdp = "v=0\r\no=- 123456789 123456789 IN IP4 127.0.0.1\r\n",
                        type = RTCSessionDescriptionType.Offer,
                    ),
                    sessionId = "test-session-id",
                ),
            ),
        )
    }

    @Test
    fun `SignalMessage with Answer payload serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.Answer,
                sessionId = "session-456",
                payload = SignalMessagePayload.RTCSessionDescription(
                    sdp = "v=0\r\no=- 987654321 987654321 IN IP4 127.0.0.1\r\n",
                    type = RTCSessionDescriptionType.Answer,
                ),
            ),
        )
    }

    @Test
    fun `SignalMessage with IceCandidate payload serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.IceCandidate,
                sessionId = "session-789",
                payload = SignalMessagePayload.RTCIceCandidate(
                    candidate = "candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host",
                    type = RTCIceCandidateType.Host,
                ),
            ),
        )
    }

    @Test
    fun `SignalMessage with TalkStreamInput payload serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.TalkStreamInput,
                sessionId = "session-talk",
                payload = SignalMessagePayload.TalkMessage(
                    content = "Test message",
                    startOfSpeech = true,
                    endOfSpeech = false,
                    correlationId = "corr-456",
                ),
            ),
        )
    }

    @Test
    fun `SignalMessage with Raw payload for EndSession serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.EndSession,
                sessionId = "session-end",
                payload = SignalMessagePayload.Raw(value = "end session"),
            ),
        )
    }

    @Test
    fun `SignalMessage with Empty payload for SessionReady serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.SessionReady,
                sessionId = "session-ready",
                payload = SignalMessagePayload.Empty,
            ),
        )
    }

    @Test
    fun `SignalMessage with Raw payload for Warning serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.Warning,
                sessionId = "session-warn",
                payload = SignalMessagePayload.Raw(value = "warning message"),
            ),
        )
    }

    @Test
    fun `SignalMessage with Heartbeat payload serializes and deserializes correctly`() {
        testSignalMessageSerialization(
            SignalMessage(
                actionType = SignalMessageType.Heartbeat,
                sessionId = "session-heartbeat",
                payload = SignalMessagePayload.Empty,
            ),
        )
    }

    private inline fun <reified T> testSerialization(original: T, serializer: KSerializer<T>) {
        val jsonString = json.encodeToString(serializer, original)
        val deserialized = json.decodeFromString(serializer, jsonString)
        assertThat(deserialized).isEqualTo(original)
    }

    private inline fun <reified T> testEnumSerialization(values: List<T>, serializer: KSerializer<T>) {
        values.forEach { value ->
            testSerialization(value, serializer)
        }
    }

    private fun testSignalMessageSerialization(message: SignalMessage) {
        testSerialization(message, SignalMessage.serializer())
    }
}
