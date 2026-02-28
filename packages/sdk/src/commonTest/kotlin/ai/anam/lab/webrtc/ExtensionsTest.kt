package ai.anam.lab.webrtc

import ai.anam.lab.Message
import ai.anam.lab.MessageRole
import ai.anam.lab.ReasoningMessage
import ai.anam.lab.ToolCallCompletedPayload
import ai.anam.lab.ToolCallFailedPayload
import ai.anam.lab.ToolCallStartedPayload
import ai.anam.lab.api.ClientConfig
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.RTCIceCredentialType
import ai.anam.lab.api.RTCIceServer
import ai.anam.lab.api.RTCSessionDescriptionType
import ai.anam.lab.api.SignalMessagePayload.RTCIceCandidate
import ai.anam.lab.api.SignalMessagePayload.RTCSessionDescription
import ai.anam.lab.fakes.toolCallCompletedMessage
import ai.anam.lab.fakes.toolCallFailedMessage
import ai.anam.lab.fakes.toolCallStartedMessage
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ExtensionsTest {

    @Test
    fun `SessionDescription toRTCSessionDescription converts correctly`() {
        val sessionDescription = SessionDescription(
            sdp = "v=0\r\no=- 123456789 123456789 IN IP4 127.0.0.1\r\n",
            type = SessionDescriptionType.Offer,
        )

        val result = sessionDescription.toRTCSessionDescription()

        assertThat(result.sdp).isEqualTo("v=0\r\no=- 123456789 123456789 IN IP4 127.0.0.1\r\n")
        assertThat(result.type).isEqualTo(RTCSessionDescriptionType.Offer)
    }

    @Test
    fun `RTCSessionDescription toSessionDescription converts correctly`() {
        val rtcSessionDescription = RTCSessionDescription(
            sdp = "v=0\r\no=- 987654321 987654321 IN IP4 127.0.0.1\r\n",
            type = RTCSessionDescriptionType.Answer,
        )

        val result = rtcSessionDescription.toSessionDescription()

        assertThat(result.sdp).isEqualTo("v=0\r\no=- 987654321 987654321 IN IP4 127.0.0.1\r\n")
        assertThat(result.type).isEqualTo(SessionDescriptionType.Answer)
    }

    @Test
    fun `SessionDescriptionType toRTCSessionDescriptionType converts all types correctly`() {
        assertThat(SessionDescriptionType.Offer.toRTCSessionDescriptionType())
            .isEqualTo(RTCSessionDescriptionType.Offer)
        assertThat(SessionDescriptionType.Answer.toRTCSessionDescriptionType())
            .isEqualTo(RTCSessionDescriptionType.Answer)
        assertThat(SessionDescriptionType.Pranswer.toRTCSessionDescriptionType())
            .isEqualTo(RTCSessionDescriptionType.ProvisionalAnswer)
        assertThat(SessionDescriptionType.Rollback.toRTCSessionDescriptionType())
            .isEqualTo(RTCSessionDescriptionType.Rollback)
    }

    @Test
    fun `RTCSessionDescriptionType toSessionDescriptionType converts all types correctly`() {
        assertThat(RTCSessionDescriptionType.Offer.toSessionDescriptionType())
            .isEqualTo(SessionDescriptionType.Offer)
        assertThat(RTCSessionDescriptionType.Answer.toSessionDescriptionType())
            .isEqualTo(SessionDescriptionType.Answer)
        assertThat(RTCSessionDescriptionType.ProvisionalAnswer.toSessionDescriptionType())
            .isEqualTo(SessionDescriptionType.Pranswer)
        assertThat(RTCSessionDescriptionType.Rollback.toSessionDescriptionType())
            .isEqualTo(SessionDescriptionType.Rollback)
    }

    @Test
    fun `IceCandidate toRTCIceCandidate converts correctly`() {
        val iceCandidate = IceCandidate(
            candidate = "candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host",
            sdpMid = "audio",
            sdpMLineIndex = 0,
        )

        val result = iceCandidate.toRTCIceCandidate()

        assertThat(result.candidate).isEqualTo("candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host")
        assertThat(result.sdpMid).isEqualTo("audio")
        assertThat(result.sdpMLineIndex).isEqualTo(0)
    }

    @Test
    fun `RTCIceCandidate toIceCandidate converts correctly with all fields`() {
        val rtcIceCandidate = RTCIceCandidate(
            candidate = "candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host",
            sdpMid = "video",
            sdpMLineIndex = 1,
        )

        val result = rtcIceCandidate.toIceCandidate()

        assertThat(result.candidate).isEqualTo("candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host")
        assertThat(result.sdpMid).isEqualTo("video")
        assertThat(result.sdpMLineIndex).isEqualTo(1)
    }

    @Test
    fun `RTCIceCandidate toIceCandidate handles null sdpMid and sdpMLineIndex`() {
        val rtcIceCandidate = RTCIceCandidate(
            candidate = "candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host",
            sdpMid = null,
            sdpMLineIndex = null,
        )

        val result = rtcIceCandidate.toIceCandidate()

        assertThat(result.candidate).isEqualTo("candidate:1 1 UDP 2130706431 192.168.1.1 54321 typ host")
        assertThat(result.sdpMid).isEqualTo("")
        assertThat(result.sdpMLineIndex).isEqualTo(0)
    }

    @Test
    fun `ClientConfig toRtcConfiguration converts correctly`() {
        val iceServer1 = RTCIceServer(
            urls = listOf("stun:stun.example.com:3478"),
            username = "user1",
            credential = "pass1",
            credentialType = RTCIceCredentialType.Password,
        )
        val iceServer2 = RTCIceServer(
            urls = listOf("turn:turn.example.com:3478"),
            username = null,
            credential = null,
            credentialType = RTCIceCredentialType.OAuth,
        )

        val clientConfig = ClientConfig(
            expectedHeartbeatIntervalSecs = 5,
            maxWsReconnectAttempts = 3,
            iceServers = listOf(iceServer1, iceServer2),
        )

        val result = clientConfig.toRtcConfiguration()

        assertThat(result.iceServers).hasSize(2)
        assertThat(result.iceServers[0].urls)
            .isEqualTo(listOf("stun:stun.example.com:3478"))
        assertThat(result.iceServers[0].username).isEqualTo("user1")
        assertThat(result.iceServers[0].password).isEqualTo("pass1")
        assertThat(result.iceServers[1].urls)
            .isEqualTo(listOf("turn:turn.example.com:3478"))
        assertThat(result.iceServers[1].username).isEqualTo("")
        assertThat(result.iceServers[1].password).isEqualTo("")
    }

    @Test
    fun `ApiIceServer toIceServer converts correctly with username and credential`() {
        val apiIceServer = RTCIceServer(
            urls = listOf("stun:stun.example.com:3478", "turn:turn.example.com:3478"),
            username = "testuser",
            credential = "testpass",
            credentialType = RTCIceCredentialType.Password,
        )

        val result = apiIceServer.toIceServer()

        assertThat(result.urls).isEqualTo(
            listOf("stun:stun.example.com:3478", "turn:turn.example.com:3478"),
        )
        assertThat(result.username).isEqualTo("testuser")
        assertThat(result.password).isEqualTo("testpass")
    }

    @Test
    fun `ApiIceServer toIceServer handles null username and credential`() {
        val apiIceServer = RTCIceServer(
            urls = listOf("stun:stun.example.com:3478"),
            username = null,
            credential = null,
            credentialType = RTCIceCredentialType.OAuth,
        )

        val result = apiIceServer.toIceServer()

        assertThat(result.urls).isEqualTo(listOf("stun:stun.example.com:3478"))
        assertThat(result.username).isEqualTo("")
        assertThat(result.password).isEqualTo("")
    }

    @Test
    fun `TextMessage toMessage converts correctly for persona role`() {
        val textMessage = DataChannelMessagePayload.TextMessage(
            id = "msg-123",
            index = 0,
            content = "Hello, world!",
            role = "persona",
            endOfSpeech = true,
            interrupted = false,
        )

        val result = textMessage.toMessage()

        assertThat(result).isEqualTo(
            Message(
                id = "persona::msg-123",
                content = "Hello, world!",
                role = MessageRole.Persona,
                endOfSpeech = true,
                interrupted = false,
                version = 1,
            ),
        )
    }

    @Test
    fun `TextMessage toMessage converts correctly for user role`() {
        val textMessage = DataChannelMessagePayload.TextMessage(
            id = "msg-456",
            index = 1,
            content = "Hi there",
            role = "user",
            endOfSpeech = false,
            interrupted = true,
        )

        val result = textMessage.toMessage()

        assertThat(result).isEqualTo(
            Message(
                id = "user::msg-456",
                content = "Hi there",
                role = MessageRole.User,
                endOfSpeech = false,
                interrupted = true,
                version = 1,
            ),
        )
    }

    @Test
    fun `ReasoningTextMessage toReasoningMessage converts correctly for persona role`() {
        val reasoningMessage = DataChannelMessagePayload.ReasoningTextMessage(
            id = "msg-123",
            index = 0,
            content = "Let me think...",
            role = "persona",
            endOfThought = false,
        )

        val result = reasoningMessage.toReasoningMessage()

        assertThat(result).isEqualTo(
            ReasoningMessage(
                id = "persona::msg-123",
                content = "Let me think...",
                role = MessageRole.Persona,
                endOfThought = false,
                version = 1,
            ),
        )
    }

    @Test
    fun `ReasoningTextMessage toReasoningMessage converts correctly for user role`() {
        val reasoningMessage = DataChannelMessagePayload.ReasoningTextMessage(
            id = "msg-456",
            index = 1,
            content = "Thinking about user input",
            role = "user",
            endOfThought = true,
        )

        val result = reasoningMessage.toReasoningMessage()

        assertThat(result).isEqualTo(
            ReasoningMessage(
                id = "user::msg-456",
                content = "Thinking about user input",
                role = MessageRole.User,
                endOfThought = true,
                version = 1,
            ),
        )
    }

    @Test
    fun `MessageRole fromString converts user correctly`() {
        val result = MessageRole.fromString("user")
        assertThat(result).isEqualTo(MessageRole.User)
    }

    @Test
    fun `MessageRole fromString converts persona correctly`() {
        val result = MessageRole.fromString("persona")
        assertThat(result).isEqualTo(MessageRole.Persona)
    }

    @Test
    fun `MessageRole fromString throws error for invalid value`() {
        assertFailsWith<IllegalStateException> {
            MessageRole.fromString("invalid")
        }
    }

    @Test
    fun `ToolCallStartedMessage toPayload converts all fields correctly`() {
        val message = toolCallStartedMessage()

        val result = message.toPayload()

        assertThat(result).isEqualTo(
            ToolCallStartedPayload(
                eventUid = "event-789",
                toolCallId = "tc-001",
                toolName = "redirect",
                toolType = "client",
                toolSubtype = null,
                arguments = mapOf("url" to "https://example.com"),
                timestamp = "2024-01-01T00:00:00Z",
                timestampUserAction = "2024-01-01T00:00:01Z",
                userActionCorrelationId = "corr-001",
            ),
        )
    }

    @Test
    fun `ToolCallStartedMessage toPayload handles non-null toolSubtype`() {
        val message = toolCallStartedMessage(toolSubtype = "webhook")

        val result = message.toPayload()

        assertThat(result.toolSubtype).isEqualTo("webhook")
    }

    @Test
    fun `ToolCallCompletedMessage toPayload converts all fields including result and executionTime`() {
        val message = toolCallCompletedMessage()

        val result = message.toPayload(executionTime = 150L)

        assertThat(result).isEqualTo(
            ToolCallCompletedPayload(
                eventUid = "event-789",
                toolCallId = "tc-001",
                toolName = "redirect",
                toolType = "server",
                toolSubtype = null,
                arguments = mapOf("url" to "https://example.com"),
                result = "success",
                executionTime = 150L,
                timestamp = "2024-01-01T00:00:00Z",
                timestampUserAction = "2024-01-01T00:00:01Z",
                userActionCorrelationId = "corr-001",
                documentsAccessed = null,
            ),
        )
    }

    @Test
    fun `ToolCallCompletedMessage toPayload handles null executionTime`() {
        val message = toolCallCompletedMessage()

        val result = message.toPayload(executionTime = null)

        assertThat(result.executionTime).isEqualTo(null)
    }

    @Test
    fun `ToolCallFailedMessage toPayload converts all fields including errorMessage and executionTime`() {
        val message = toolCallFailedMessage()

        val result = message.toPayload(executionTime = 200L)

        assertThat(result).isEqualTo(
            ToolCallFailedPayload(
                eventUid = "event-789",
                toolCallId = "tc-001",
                toolName = "redirect",
                toolType = "server",
                toolSubtype = null,
                arguments = mapOf("url" to "https://example.com"),
                errorMessage = "Tool execution timed out",
                executionTime = 200L,
                timestamp = "2024-01-01T00:00:00Z",
                timestampUserAction = "2024-01-01T00:00:01Z",
                userActionCorrelationId = "corr-001",
            ),
        )
    }

    @Test
    fun `ToolCallStartedMessage toPayload converts nested arguments correctly`() {
        val arguments = buildJsonObject {
            put("url", "https://example.com")
            put("count", 42)
            put("active", true)
            put("nested", buildJsonObject { put("key", "value") })
            put(
                "items",
                buildJsonArray {
                    add(JsonPrimitive("a"))
                    add(JsonPrimitive("b"))
                },
            )
            put("nullable", JsonNull)
        }
        val message = toolCallStartedMessage(arguments = arguments)

        val result = message.toPayload()

        assertThat(result.arguments).isEqualTo(
            mapOf(
                "url" to "https://example.com",
                "count" to 42L,
                "active" to true,
                "nested" to mapOf("key" to "value"),
                "items" to listOf("a", "b"),
                "nullable" to null,
            ),
        )
    }
}
