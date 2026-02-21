package ai.anam.lab.webrtc

import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.fakes.FakeLogger
import ai.anam.lab.fakes.FakeStreamingClient
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ToolClientTest {
    private val logger = FakeLogger()
    private val fakeStreamingClient = FakeStreamingClient()
    private val toolClient = ToolClientImpl(fakeStreamingClient, logger)

    @Test
    fun `single tool event is emitted correctly`() = runTest {
        toolClient.toolEvents.test {
            val message = clientToolMessage()
            emitClientToolMessage(message)

            val event = awaitItem()
            assertThat(event).isEqualTo(message.toToolEvent())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple tool events are emitted as discrete events`() = runTest {
        toolClient.toolEvents.test {
            val message1 = clientToolMessage(id = "event-1")
            val message2 = clientToolMessage(id = "event-2")

            emitClientToolMessage(message1)
            assertThat(awaitItem()).isEqualTo(message1.toToolEvent())

            emitClientToolMessage(message2)
            assertThat(awaitItem()).isEqualTo(message2.toToolEvent())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `non-ClientToolEvent messages are filtered out`() = runTest {
        toolClient.toolEvents.test {
            val textMessage = DataChannelMessagePayload.TextMessage(
                id = "msg-1",
                index = 0,
                content = "Hello",
                role = "persona",
                endOfSpeech = true,
                interrupted = false,
            )
            val toolMessage = clientToolMessage()

            // Emit a non-ClientToolEvent message first - it should be filtered out
            fakeStreamingClient.emitDataChannelMessage(
                DataChannelMessage(type = DataChannelMessageType.SpeechText, data = textMessage),
            )

            // Emit a ClientToolEvent - this should be the only one that appears
            emitClientToolMessage(toolMessage)

            // Only the tool event should appear
            assertThat(awaitItem()).isEqualTo(toolMessage.toToolEvent())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `tool event fields are correctly mapped`() = runTest {
        toolClient.toolEvents.test {
            val message = clientToolMessage(
                id = "uid-123",
                sessionId = "session-456",
                name = "redirect",
                data = """{"url": "https://example.com"}""",
                timestamp = "2024-01-01T00:00:00",
                targetTimestamp = "2024-01-01T00:00:01",
                correlationId = "corr-789",
            )
            emitClientToolMessage(message)

            val event = awaitItem()
            assertThat(event.eventUid).isEqualTo("uid-123")
            assertThat(event.sessionId).isEqualTo("session-456")
            assertThat(event.eventName).isEqualTo("redirect")
            assertThat(event.eventData).isEqualTo("""{"url": "https://example.com"}""")
            assertThat(event.timestamp).isEqualTo("2024-01-01T00:00:00")
            assertThat(event.timestampUserAction).isEqualTo("2024-01-01T00:00:01")
            assertThat(event.userActionCorrelationId).isEqualTo("corr-789")

            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun emitClientToolMessage(data: DataChannelMessagePayload.ClientToolMessage) {
        fakeStreamingClient.emitDataChannelMessage(
            DataChannelMessage(type = DataChannelMessageType.ClientToolEvent, data = data),
        )
    }

    private fun clientToolMessage(
        id: String = "event-1",
        sessionId: String = "session-1",
        name: String = "test-tool",
        data: String = "{}",
        timestamp: String = "2024-01-01T00:00:00",
        targetTimestamp: String = "2024-01-01T00:00:00",
        correlationId: String = "corr-1",
        usedOutsideEngine: Boolean = false,
    ) = DataChannelMessagePayload.ClientToolMessage(
        id = id,
        sessionId = sessionId,
        name = name,
        data = data,
        timestamp = timestamp,
        targetTimestamp = targetTimestamp,
        correlationId = correlationId,
        usedOutsideEngine = usedOutsideEngine,
    )
}
