package ai.anam.lab.webrtc

import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.fakes.FakeLogger
import ai.anam.lab.fakes.FakeStreamingClient
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class ReasoningClientTest {
    private val logger = FakeLogger()
    private val fakeStreamingClient = FakeStreamingClient()
    private val reasoningClient = ReasoningClientImpl(fakeStreamingClient, logger)

    @Test
    fun `single message is emitted correctly`() = runTest {
        reasoningClient.reasoningMessages.test {
            val message = reasoningTextMessage(content = "Let me think")
            emitReasoningTextMessage(message)

            val messages = awaitItem()
            assertThat(messages).isEqualTo(listOf(message.toReasoningMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple different messages are emitted correctly`() = runTest {
        reasoningClient.reasoningMessages.test {
            val message1 = reasoningTextMessage(content = "First thought", id = "msg-1")
            val message2 = reasoningTextMessage(content = "Second thought", id = "msg-2")

            emitReasoningTextMessage(message1)
            val firstMessages = awaitItem()
            assertThat(firstMessages).isEqualTo(listOf(message1.toReasoningMessage()))

            emitReasoningTextMessage(message2)
            val secondMessages = awaitItem()
            assertThat(secondMessages).isEqualTo(
                listOf(message1.toReasoningMessage(), message2.toReasoningMessage()),
            )

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `message updates accumulate content and increment version`() = runTest {
        reasoningClient.reasoningMessages.test {
            val initialMessage = reasoningTextMessage(content = "Let me")
            val updateMessage = reasoningTextMessage(content = " think", index = 1, endOfThought = true)

            emitReasoningTextMessage(initialMessage)
            val firstMessages = awaitItem()
            assertThat(firstMessages).isEqualTo(listOf(initialMessage.toReasoningMessage()))

            emitReasoningTextMessage(updateMessage)
            val secondMessages = awaitItem()
            assertThat(secondMessages).hasSize(1)
            assertThat(secondMessages[0].content).isEqualTo("Let me think")
            assertThat(secondMessages[0].version).isEqualTo(2)
            assertThat(secondMessages[0].endOfThought).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `non-ReasoningTextMessage payloads are filtered out`() = runTest {
        reasoningClient.reasoningMessages.test {
            val textMessage = DataChannelMessagePayload.TextMessage(
                id = "msg-1",
                index = 0,
                content = "Hello",
                role = "persona",
                endOfSpeech = true,
                interrupted = false,
            )
            val reasoningMessage = reasoningTextMessage(content = "Thinking...")

            // Emit a non-ReasoningTextMessage payload first - it should be filtered out
            fakeStreamingClient.emitDataChannelMessage(
                DataChannelMessage(type = DataChannelMessageType.SpeechText, data = textMessage),
            )

            // Emit a ReasoningTextMessage - this should be the only one that appears
            emitReasoningTextMessage(reasoningMessage)

            // Only the ReasoningTextMessage should appear
            val messages = awaitItem()
            assertThat(messages).isEqualTo(listOf(reasoningMessage.toReasoningMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple updates to same message accumulate correctly`() = runTest {
        reasoningClient.reasoningMessages.test {
            val messages = listOf(
                reasoningTextMessage(content = "Let"),
                reasoningTextMessage(content = " me", index = 1),
                reasoningTextMessage(content = " think", index = 2, endOfThought = true),
            )

            emitReasoningTextMessage(messages[0])
            val firstUpdate = awaitItem()
            assertThat(firstUpdate[0]).isEqualTo(messages[0].toReasoningMessage())

            emitReasoningTextMessage(messages[1])
            val secondUpdate = awaitItem()
            assertThat(secondUpdate[0].content).isEqualTo("Let me")
            assertThat(secondUpdate[0].version).isEqualTo(2)

            emitReasoningTextMessage(messages[2])
            val thirdUpdate = awaitItem()
            assertThat(thirdUpdate[0].content).isEqualTo("Let me think")
            assertThat(thirdUpdate[0].version).isEqualTo(3)
            assertThat(thirdUpdate[0].endOfThought).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun emitReasoningTextMessage(data: DataChannelMessagePayload.ReasoningTextMessage) {
        fakeStreamingClient.emitDataChannelMessage(
            DataChannelMessage(type = DataChannelMessageType.ReasoningText, data = data),
        )
    }

    private fun reasoningTextMessage(
        content: String,
        id: String = "msg-1",
        index: Int = 0,
        role: String = "persona",
        endOfThought: Boolean = false,
    ) = DataChannelMessagePayload.ReasoningTextMessage(
        id = id,
        index = index,
        content = content,
        role = role,
        endOfThought = endOfThought,
    )
}
