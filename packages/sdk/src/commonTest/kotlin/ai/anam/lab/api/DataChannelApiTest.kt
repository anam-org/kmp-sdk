package ai.anam.lab.api

import assertk.assertThat
import assertk.assertions.isEqualTo
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
    fun `ClientToolMessage serializes and deserializes correctly`() {
        testSerialization(
            DataChannelMessage(
                type = DataChannelMessageType.ClientToolEvent,
                data = DataChannelMessagePayload.ClientToolMessage(
                    id = "event-456",
                    sessionId = "session-789",
                    name = "redirect",
                    data = """{"url":"https://example.com"}""",
                    timestamp = "2024-01-01T00:00:00Z",
                    targetTimestamp = "2024-01-01T00:00:01Z",
                    correlationId = "corr-001",
                    usedOutsideEngine = true,
                ),
            ),
        )
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
