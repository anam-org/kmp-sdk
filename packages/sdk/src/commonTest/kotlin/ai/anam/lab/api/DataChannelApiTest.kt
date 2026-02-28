package ai.anam.lab.api

import ai.anam.lab.fakes.toolCallCompletedMessage
import ai.anam.lab.fakes.toolCallFailedMessage
import ai.anam.lab.fakes.toolCallStartedMessage
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.serialization.SerializationException

class DataChannelApiTest {

    // Let's use the default JSON configuration.
    private val json = defaultJsonConfiguration

    @Test
    fun `TextMessage serializes and deserializes correctly`() {
        testSerialization(
            DataChannelMessage(
                type = DataChannelMessageType.SpeechText,
                data = DataChannelMessagePayload.TextMessage(
                    id = "msg-123",
                    index = 1,
                    content = "Hello from data channel",
                    role = "assistant",
                    endOfSpeech = true,
                    interrupted = false,
                ),
            ),
        )
    }

    @Test
    fun `ReasoningTextMessage serializes and deserializes correctly`() {
        testSerialization(
            DataChannelMessage(
                type = DataChannelMessageType.ReasoningText,
                data = DataChannelMessagePayload.ReasoningTextMessage(
                    id = "msg-456",
                    index = 0,
                    content = "Let me think about this",
                    role = "persona",
                    endOfThought = false,
                ),
            ),
        )
    }

    @Test
    fun `ReasoningTextMessage deserializes correctly from raw JSON`() {
        val rawJson = """
            {
                "messageType": "reasoningText",
                "data": {
                    "message_id": "msg-1",
                    "content_index": 0,
                    "content": "thinking",
                    "role": "persona",
                    "end_of_thought": false
                }
            }
        """.trimIndent()
        val result = json.decodeFromString(DataChannelMessage.serializer(), rawJson)
        assertThat(result.type).isEqualTo(DataChannelMessageType.ReasoningText)
        assertThat(result.data)
            .isInstanceOf<DataChannelMessagePayload.ReasoningTextMessage>()

        val reasoning = result.data as DataChannelMessagePayload.ReasoningTextMessage
        assertThat(reasoning.id).isEqualTo("msg-1")
        assertThat(reasoning.index).isEqualTo(0)
        assertThat(reasoning.content).isEqualTo("thinking")
        assertThat(reasoning.role).isEqualTo("persona")
        assertThat(reasoning.endOfThought).isEqualTo(false)
    }

    @Test
    fun `Unknown payload throws SerializationException`() {
        val rawJson = """{ "unknown_key": "value" }"""

        assertFailsWith<SerializationException> {
            json.decodeFromString(DataChannelMessage.serializer(), rawJson)
        }
    }

    @Test
    fun `UserTextMessage serializes and deserializes correctly`() {
        testSerialization(
            UserDataMessage.UserTextMessage(
                content = "Hello from user",
                sessionId = "session-id",
                timestamp = "now",
            ),
        )
    }

    @Test
    fun `PersonaInterruptMessage serializes and deserializes correctly`() {
        testSerialization(
            UserDataMessage.PersonaInterruptMessage(
                sessionId = "session-id",
                timestamp = "now",
            ),
        )
    }

    // region Tool Call Serialization

    @Test
    fun `ToolCallStartedMessage serializes and deserializes correctly`() {
        testSerialization(
            DataChannelMessage(
                type = DataChannelMessageType.ToolCallStarted,
                data = toolCallStartedMessage(),
            ),
        )
    }

    @Test
    fun `ToolCallCompletedMessage serializes and deserializes correctly`() {
        testSerialization(
            DataChannelMessage(
                type = DataChannelMessageType.ToolCallCompleted,
                data = toolCallCompletedMessage(),
            ),
        )
    }

    @Test
    fun `ToolCallFailedMessage serializes and deserializes correctly`() {
        testSerialization(
            DataChannelMessage(
                type = DataChannelMessageType.ToolCallFailed,
                data = toolCallFailedMessage(),
            ),
        )
    }

    @Test
    fun `ToolCallStartedMessage handles optional toolSubtype as null`() {
        val message = toolCallStartedMessage(toolSubtype = null)
        val dataChannelMessage = DataChannelMessage(
            type = DataChannelMessageType.ToolCallStarted,
            data = message,
        )

        val jsonString = json.encodeToString(DataChannelMessage.serializer(), dataChannelMessage)
        val deserialized = json.decodeFromString(DataChannelMessage.serializer(), jsonString)

        val payload = deserialized.data as DataChannelMessagePayload.ToolCallStartedMessage
        assertThat(payload.toolSubtype).isEqualTo(null)
    }

    @Test
    fun `polymorphic serializer selects ToolCallStartedMessage for tool_call_id without result or error`() {
        val rawJson = """
            {
                "messageType": "toolCallStarted",
                "data": {
                    "event_uid": "e-1",
                    "session_id": "s-1",
                    "tool_call_id": "tc-1",
                    "tool_name": "redirect",
                    "tool_type": "client",
                    "arguments": {"url": "https://example.com"},
                    "timestamp": "2024-01-01T00:00:00Z",
                    "timestamp_user_action": "2024-01-01T00:00:01Z",
                    "user_action_correlation_id": "corr-1",
                    "used_outside_engine": true
                }
            }
        """.trimIndent()

        val deserialized = json.decodeFromString(DataChannelMessage.serializer(), rawJson)
        assertThat(deserialized.data).isInstanceOf<DataChannelMessagePayload.ToolCallStartedMessage>()
    }

    @Test
    fun `polymorphic serializer selects ToolCallCompletedMessage when result is present`() {
        val rawJson = """
            {
                "messageType": "toolCallCompleted",
                "data": {
                    "event_uid": "e-1",
                    "session_id": "s-1",
                    "tool_call_id": "tc-1",
                    "tool_name": "redirect",
                    "tool_type": "server",
                    "arguments": {},
                    "timestamp": "2024-01-01T00:00:00Z",
                    "timestamp_user_action": "2024-01-01T00:00:01Z",
                    "user_action_correlation_id": "corr-1",
                    "used_outside_engine": true,
                    "result": "ok"
                }
            }
        """.trimIndent()

        val deserialized = json.decodeFromString(DataChannelMessage.serializer(), rawJson)
        assertThat(deserialized.data).isInstanceOf<DataChannelMessagePayload.ToolCallCompletedMessage>()
    }

    @Test
    fun `polymorphic serializer selects ToolCallFailedMessage when error_message is present`() {
        val rawJson = """
            {
                "messageType": "toolCallFailed",
                "data": {
                    "event_uid": "e-1",
                    "session_id": "s-1",
                    "tool_call_id": "tc-1",
                    "tool_name": "redirect",
                    "tool_type": "server",
                    "arguments": {},
                    "timestamp": "2024-01-01T00:00:00Z",
                    "timestamp_user_action": "2024-01-01T00:00:01Z",
                    "user_action_correlation_id": "corr-1",
                    "used_outside_engine": true,
                    "error_message": "timeout"
                }
            }
        """.trimIndent()

        val deserialized = json.decodeFromString(DataChannelMessage.serializer(), rawJson)
        assertThat(deserialized.data).isInstanceOf<DataChannelMessagePayload.ToolCallFailedMessage>()
    }

    // endregion

    private fun testSerialization(message: DataChannelMessage) {
        val jsonString = json.encodeToString(DataChannelMessage.serializer(), message)
        val deserialized = json.decodeFromString(DataChannelMessage.serializer(), jsonString)
        assertThat(deserialized).isEqualTo(message)
    }

    private fun testSerialization(message: UserDataMessage) {
        val jsonString = json.encodeToString(UserDataMessage.serializer(), message)
        val deserialized = json.decodeFromString(UserDataMessage.serializer(), jsonString)
        assertThat(deserialized).isEqualTo(message)
    }
}
