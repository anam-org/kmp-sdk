package ai.anam.lab.webrtc

import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.fakes.FakeLogger
import ai.anam.lab.fakes.FakeStreamingClient
import ai.anam.lab.fakes.toolCallStartedMessage
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class MessagingClientTest {
    private val logger = FakeLogger()
    private val streamingClient = FakeStreamingClient()
    private val messagingClient = MessagingClientImpl(streamingClient, logger)

    @Test
    fun `single message is emitted correctly`() = runTest {
        messagingClient.messages.test {
            val textMessage = textMessage(content = "Hello")
            emitTextMessage(textMessage)

            val messages = awaitItem()
            assertThat(messages).isEqualTo(listOf(textMessage.toMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple different messages are emitted correctly`() = runTest {
        messagingClient.messages.test {
            val message1 = textMessage(content = "Hello", id = "msg-1", role = "user")
            val message2 = textMessage(content = "Hi there", id = "msg-2")

            emitTextMessage(message1)
            val firstMessages = awaitItem()
            assertThat(firstMessages).isEqualTo(listOf(message1.toMessage()))

            emitTextMessage(message2)
            val secondMessages = awaitItem()
            assertThat(secondMessages).isEqualTo(listOf(message1.toMessage(), message2.toMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `message updates accumulate content and increment version`() = runTest {
        messagingClient.messages.test {
            val initialMessage = textMessage(content = "Hello", endOfSpeech = false)
            val updateMessage = textMessage(content = " world", index = 1)

            emitTextMessage(initialMessage)
            val firstMessages = awaitItem()
            assertThat(firstMessages).isEqualTo(listOf(initialMessage.toMessage()))

            emitTextMessage(updateMessage)
            val secondMessages = awaitItem()
            assertThat(secondMessages).hasSize(1)
            assertThat(secondMessages[0].content).isEqualTo("Hello world")
            assertThat(secondMessages[0].version).isEqualTo(2)
            assertThat(secondMessages[0].endOfSpeech).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `non-TextMessage payloads are filtered out`() = runTest {
        messagingClient.messages.test {
            val textMessage = textMessage(content = "Hello")

            // Emit a non-TextMessage payload first - it should be filtered out
            streamingClient.emitDataChannelMessage(
                DataChannelMessage(
                    type = DataChannelMessageType.ToolCallStarted,
                    data = toolCallStartedMessage(),
                ),
            )

            // Emit a TextMessage - this should be the only one that appears
            emitTextMessage(textMessage)

            // Only the TextMessage should appear, not the ToolCallStartedMessage
            val messages = awaitItem()
            assertThat(messages).isEqualTo(listOf(textMessage.toMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `interrupted flag is updated correctly`() = runTest {
        messagingClient.messages.test {
            val initialMessage = textMessage(content = "Hello", endOfSpeech = false)
            val interruptedMessage = textMessage(content = " world", index = 1, endOfSpeech = false, interrupted = true)

            emitTextMessage(initialMessage)
            val firstMessages = awaitItem()
            assertThat(firstMessages[0]).isEqualTo(initialMessage.toMessage())

            emitTextMessage(interruptedMessage)
            val secondMessages = awaitItem()
            // After accumulation, the message should have the interrupted flag set to true
            assertThat(secondMessages[0].interrupted).isTrue()
            assertThat(secondMessages[0].content).isEqualTo("Hello world")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple updates to same message accumulate correctly`() = runTest {
        messagingClient.messages.test {
            val messages = listOf(
                textMessage(content = "Hello", endOfSpeech = false),
                textMessage(content = " there", index = 1, endOfSpeech = false),
                textMessage(content = " world", index = 2),
            )

            emitTextMessage(messages[0])
            val firstUpdate = awaitItem()
            assertThat(firstUpdate[0]).isEqualTo(messages[0].toMessage())

            emitTextMessage(messages[1])
            val secondUpdate = awaitItem()
            assertThat(secondUpdate[0].content).isEqualTo("Hello there")
            assertThat(secondUpdate[0].version).isEqualTo(2)

            emitTextMessage(messages[2])
            val thirdUpdate = awaitItem()
            assertThat(thirdUpdate[0].content).isEqualTo("Hello there world")
            assertThat(thirdUpdate[0].version).isEqualTo(3)
            assertThat(thirdUpdate[0].endOfSpeech).isTrue()

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `user role messages are handled correctly`() = runTest {
        messagingClient.messages.test {
            val userMessage = textMessage(content = "User message", role = "user")
            emitTextMessage(userMessage)

            val messages = awaitItem()
            assertThat(messages).isEqualTo(listOf(userMessage.toMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `persona role messages are handled correctly`() = runTest {
        messagingClient.messages.test {
            val personaMessage = textMessage(content = "Persona message")
            emitTextMessage(personaMessage)

            val messages = awaitItem()
            assertThat(messages).isEqualTo(listOf(personaMessage.toMessage()))

            cancelAndIgnoreRemainingEvents()
        }
    }

    private suspend fun emitTextMessage(data: DataChannelMessagePayload.TextMessage) {
        streamingClient.emitDataChannelMessage(
            DataChannelMessage(type = DataChannelMessageType.SpeechText, data = data),
        )
    }

    private fun textMessage(
        content: String,
        id: String = "msg-1",
        index: Int = 0,
        role: String = "persona",
        endOfSpeech: Boolean = true,
        interrupted: Boolean = false,
    ) = DataChannelMessagePayload.TextMessage(
        id = id,
        index = index,
        content = content,
        role = role,
        endOfSpeech = endOfSpeech,
        interrupted = interrupted,
    )
}
