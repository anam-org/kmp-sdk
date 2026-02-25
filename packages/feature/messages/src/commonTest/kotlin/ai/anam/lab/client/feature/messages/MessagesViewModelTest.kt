package ai.anam.lab.client.feature.messages

import ai.anam.lab.Message
import ai.anam.lab.MessageRole
import ai.anam.lab.client.core.test.FakeLogger
import ai.anam.lab.client.domain.session.ObserveActiveMessageHistoryInteractor
import ai.anam.lab.client.domain.session.ObserveIsSessionActiveInteractor
import ai.anam.lab.client.domain.session.SendActiveSessionUserMessageInteractor
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class MessagesViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // region Init / Observation

    @Test
    fun `initial state has empty messages and inactive session`() = testScope.runTest {
        val viewModel = createViewModel()

        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.messages).isEmpty()
            assertThat(state.isActive).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `messages update when observed message history changes`() = testScope.runTest {
        val messageFlow = MutableSharedFlow<List<Message>>()
        val viewModel = createViewModel(messageHistoryFlow = messageFlow)

        val msg1 = testMessage("1", "Hello", MessageRole.User)
        val msg2 = testMessage("2", "Hi there", MessageRole.Persona)

        viewModel.state.test {
            assertThat(awaitItem().messages).isEmpty()

            messageFlow.emit(listOf(msg1, msg2))

            assertThat(awaitItem().messages).containsExactly(msg1, msg2)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `isActive updates when observed session active state changes`() = testScope.runTest {
        val isActiveFlow = MutableSharedFlow<Boolean>()
        val viewModel = createViewModel(isSessionActiveFlow = isActiveFlow)

        viewModel.state.test {
            assertThat(awaitItem().isActive).isFalse()

            isActiveFlow.emit(true)

            assertThat(awaitItem().isActive).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Send Message

    @Test
    fun `sendUserMessage calls interactor with content`() = testScope.runTest {
        var sentContent: String? = null
        val viewModel = createViewModel(
            onSendMessage = { content -> sentContent = content },
        )

        viewModel.sendUserMessage("Hello world")
        runCurrent()

        assertThat(sentContent).isEqualTo("Hello world")
    }

    @Test
    fun `sendUserMessage does not crash on interactor failure`() = testScope.runTest {
        val viewModel = createViewModel(
            onSendMessage = { throw RuntimeException("Network error") },
        )

        viewModel.sendUserMessage("Hello world")
        runCurrent()

        // No exception thrown — runCatching in the ViewModel swallows it
    }

    // endregion

    // region Helpers

    private fun createViewModel(
        messageHistoryFlow: Flow<List<Message>> = emptyFlow(),
        isSessionActiveFlow: Flow<Boolean> = emptyFlow(),
        onSendMessage: suspend (String) -> Unit = {},
    ): MessagesViewModel = MessagesViewModel(
        observeActiveMessageHistoryInteractor = ObserveActiveMessageHistoryInteractor { messageHistoryFlow },
        observeIsSessionActiveInteractor = ObserveIsSessionActiveInteractor { isSessionActiveFlow },
        sendActiveSessionUserMessageInteractor = SendActiveSessionUserMessageInteractor { content ->
            onSendMessage(content)
        },
        logger = FakeLogger(),
    ).also { testScope.runCurrent() }

    private fun testMessage(id: String, content: String, role: MessageRole) = Message(
        id = id,
        content = content,
        role = role,
        endOfSpeech = true,
        interrupted = false,
    )

    // endregion
}
