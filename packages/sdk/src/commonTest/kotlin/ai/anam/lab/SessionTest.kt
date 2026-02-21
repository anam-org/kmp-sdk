package ai.anam.lab

import ai.anam.lab.api.UserDataMessage
import ai.anam.lab.fakes.FakeLogger
import ai.anam.lab.fakes.FakeMediaStreamManager
import ai.anam.lab.fakes.FakeMessagingClient
import ai.anam.lab.fakes.FakePlatformSessionManager
import ai.anam.lab.fakes.FakeSignallingClient
import ai.anam.lab.fakes.FakeStreamingClient
import ai.anam.lab.fakes.FakeToolClient
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SessionTest {
    private val testDispatcher = StandardTestDispatcher()
    private val logger = FakeLogger()
    private val signallingClient = FakeSignallingClient()
    private val streamingClient = FakeStreamingClient()
    private val mediaStreamManager = FakeMediaStreamManager()
    private val messagingClient = FakeMessagingClient()
    private val toolClient = FakeToolClient()
    private val platformSessionManager = FakePlatformSessionManager()

    @Test
    fun `session id is correctly set`() = runTest(testDispatcher) {
        val id = "test-session-123"
        val session = withSession(sessionId = id)

        assertThat(session.id).isEqualTo(id)
    }

    @Test
    fun `start connects all clients and starts platform session manager`() = runTest(testDispatcher) {
        withRunningSession { _ ->
            // Verify platform session manager is started.
            assertThat(platformSessionManager.isStarted).isTrue()

            // Verify signalling client is connected.
            signallingClient.connected.test {
                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }

            // Verify streaming client is connected.
            streamingClient.connected.test {
                assertThat(awaitItem()).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `events flow merges events from signalling client`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                val testEvent = SessionEvent.ConnectionEstablished
                signallingClient.emitEvent(testEvent)

                assertThat(awaitItem()).isEqualTo(testEvent)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `events flow merges events from streaming client`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                val testEvent = SessionEvent.InputAudioStreamStarted
                streamingClient.emitEvent(testEvent)

                assertThat(awaitItem()).isEqualTo(testEvent)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `events flow merges local events`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                session.onFirstFrameRendered()

                assertThat(awaitItem()).isEqualTo(SessionEvent.VideoPlayStarted)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `events flow merges events from multiple sources`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                val signallingEvent = SessionEvent.ConnectionEstablished
                val streamingEvent = SessionEvent.InputAudioStreamStarted
                val localEvent = SessionEvent.VideoPlayStarted

                signallingClient.emitEvent(signallingEvent)
                assertThat(awaitItem()).isEqualTo(signallingEvent)

                streamingClient.emitEvent(streamingEvent)
                assertThat(awaitItem()).isEqualTo(streamingEvent)

                session.onFirstFrameRendered()
                assertThat(awaitItem()).isEqualTo(localEvent)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `events flow emits ConnectionClosed event`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                // Skip the initial connecting event if present (depends on implementation detail)
                // But in our case, we just emit the closed event from the client
                val closedEvent = SessionEvent.ConnectionClosed(ConnectionClosedReason.Normal)
                signallingClient.emitEvent(closedEvent)

                // We might see other events first depending on the exact timing/setup,
                // but we should eventually see the Closed event.
                // Since we are just emitting one event in this isolated test:
                assertThat(awaitItem()).isEqualTo(closedEvent)
                awaitComplete() // The flow should complete after ConnectionClosed
            }
        }
    }

    @Test
    fun `session stops if signalling client disconnects with error`() = runTest(testDispatcher) {
        val session = withSession()
        val job = launch { session.start() }
        runCurrent()

        // Simulate an error from the signalling client
        val errorReason = ConnectionClosedReason.SignallingClientConnectionFailure("Test Error")
        signallingClient.emitEvent(SessionEvent.ConnectionClosed(errorReason))

        runCurrent()

        assertThat(session.isActive).isFalse()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `messages flow exposes messages from messaging client`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.messages.test {
                assertThat(awaitItem()).isEqualTo(emptyList())

                val testMessages = listOf(withMessage(content = "Hello"))

                messagingClient.emitMessages(testMessages)

                assertThat(awaitItem()).isEqualTo(testMessages)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `messages flow handles multiple message updates`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.messages.test {
                assertThat(awaitItem()).isEqualTo(emptyList())

                val firstMessages = listOf(withMessage(content = "Hello", endOfSpeech = false))

                messagingClient.emitMessages(firstMessages)
                assertThat(awaitItem()).isEqualTo(firstMessages)

                val secondMessages = listOf(withMessage(content = " world", id = "msg-1"))

                messagingClient.emitMessages(secondMessages)
                assertThat(awaitItem()).isEqualTo(secondMessages)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `toolEvents flow exposes events from tool client`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.toolEvents.test {
                val testEvent = ToolEvent(
                    eventUid = "event-1",
                    sessionId = "session-1",
                    eventName = "redirect",
                    eventData = """{"url": "https://example.com"}""",
                    timestamp = "2024-01-01T00:00:00",
                    timestampUserAction = "2024-01-01T00:00:01",
                    userActionCorrelationId = "corr-1",
                )

                toolClient.emitToolEvent(testEvent)

                assertThat(awaitItem()).isEqualTo(testEvent)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `tool events are emitted as SessionEvent ToolCall in events flow`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                val testEvent = ToolEvent(
                    eventUid = "event-1",
                    sessionId = "session-1",
                    eventName = "redirect",
                    eventData = "{}",
                    timestamp = "2024-01-01T00:00:00",
                    timestampUserAction = "2024-01-01T00:00:00",
                    userActionCorrelationId = "corr-1",
                )

                toolClient.emitToolEvent(testEvent)

                assertThat(awaitItem()).isEqualTo(SessionEvent.ToolCall(testEvent))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `multiple tool events are emitted as discrete events`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.toolEvents.test {
                val event1 = ToolEvent(
                    eventUid = "event-1",
                    sessionId = "session-1",
                    eventName = "redirect",
                    eventData = "{}",
                    timestamp = "2024-01-01T00:00:00",
                    timestampUserAction = "2024-01-01T00:00:00",
                    userActionCorrelationId = "corr-1",
                )
                val event2 = ToolEvent(
                    eventUid = "event-2",
                    sessionId = "session-1",
                    eventName = "show-modal",
                    eventData = "{}",
                    timestamp = "2024-01-01T00:00:01",
                    timestampUserAction = "2024-01-01T00:00:01",
                    userActionCorrelationId = "corr-2",
                )

                toolClient.emitToolEvent(event1)
                assertThat(awaitItem()).isEqualTo(event1)

                toolClient.emitToolEvent(event2)
                assertThat(awaitItem()).isEqualTo(event2)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `onFirstFrameRendered emits VideoPlayStarted event`() = runTest(testDispatcher) {
        withRunningSession { session ->
            session.events.test {
                session.onFirstFrameRendered()

                assertThat(awaitItem()).isEqualTo(SessionEvent.VideoPlayStarted)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `session terminates when ConnectionClosed event is received`() = runTest(testDispatcher) {
        val session = withSession()
        val job = launch {
            session.start()
        }

        runCurrent()
        assertThat(session.isActive).isTrue()

        // Emit ConnectionClosed event
        signallingClient.emitEvent(SessionEvent.ConnectionClosed(ConnectionClosedReason.Normal))
        runCurrent()

        // Verify session is no longer active and job is completed
        assertThat(session.isActive).isFalse()
        assertThat(job.isCompleted).isTrue()
    }

    @Test
    fun `resources are released when session is cancelled`() = runTest(testDispatcher) {
        val session = withSession()
        val job = launch {
            session.start()
        }

        runCurrent()
        job.cancelAndJoin()

        assertThat(mediaStreamManager.isReleased).isTrue()
    }

    @Test
    fun `resources are released when session terminates normally`() = runTest(testDispatcher) {
        val session = withSession()
        val job = launch {
            session.start()
        }

        runCurrent()
        signallingClient.emitEvent(SessionEvent.ConnectionClosed(ConnectionClosedReason.Normal))
        runCurrent()

        assertThat(mediaStreamManager.isReleased).isTrue()
    }

    @Test
    fun `platform session manager is stopped when session is cancelled`() = runTest(testDispatcher) {
        val session = withSession()
        val job = launch {
            session.start()
        }

        runCurrent()
        assertThat(platformSessionManager.isStarted).isTrue()

        job.cancelAndJoin()

        assertThat(platformSessionManager.isStarted).isFalse()
    }

    @Test
    fun `isActive is false when session is created`() = runTest(testDispatcher) {
        val session = withSession()
        assertThat(session.isActive).isFalse()
    }

    @Test
    fun `isActive is true when session is started`() = runTest(testDispatcher) {
        withRunningSession { session ->
            assertThat(session.isActive).isTrue()
        }
    }

    @Test
    fun `isActive is false when session is cancelled`() = runTest(testDispatcher) {
        val session = withSession()
        val job = launch {
            session.start()
        }

        runCurrent()
        assertThat(session.isActive).isTrue()

        job.cancelAndJoin()

        assertThat(session.isActive).isFalse()
    }

    @Test
    fun `sendUserMessage sends correct UserTextMessage when session is active`() = runTest(testDispatcher) {
        val sessionId = "test-session-123"
        withRunningSession(sessionId = sessionId) { session ->
            val messageContent = "Hello, world!"

            session.sendUserMessage(messageContent)

            assertThat(streamingClient.sentMessages.size).isEqualTo(1)
            val sentMessage = streamingClient.sentMessages[0] as UserDataMessage.UserTextMessage
            val expectedMessage = UserDataMessage.UserTextMessage(
                content = messageContent,
                sessionId = sessionId,
                timestamp = sentMessage.timestamp,
            )
            assertThat(sentMessage).isEqualTo(expectedMessage)
        }
    }

    @Test
    fun `sendUserMessage returns false when session is not active`() = runTest(testDispatcher) {
        val session = withSession()

        val result = session.sendUserMessage("Hello")

        assertThat(result).isFalse()
        assertThat(streamingClient.sentMessages.size).isEqualTo(0)
    }

    @Test
    fun `interruptPersona sends correct PersonaInterruptMessage when session is active`() = runTest(testDispatcher) {
        val sessionId = "test-session-456"
        withRunningSession(sessionId = sessionId) { session ->
            session.interruptPersona()

            assertThat(streamingClient.sentMessages.size).isEqualTo(1)
            val sentMessage = streamingClient.sentMessages[0] as UserDataMessage.PersonaInterruptMessage
            val expectedMessage = UserDataMessage.PersonaInterruptMessage(
                sessionId = sessionId,
                timestamp = sentMessage.timestamp,
            )
            assertThat(sentMessage).isEqualTo(expectedMessage)
        }
    }

    @Test
    fun `interruptPersona returns false when session is not active`() = runTest(testDispatcher) {
        val session = withSession()

        val result = session.interruptPersona()

        assertThat(result).isFalse()
        assertThat(streamingClient.sentMessages.size).isEqualTo(0)
    }

    @Test
    fun `isLocalAudioMuted defaults to false`() = runTest(testDispatcher) {
        val session = withSession()
        assertThat(session.isLocalAudioMuted).isFalse()
    }

    @Test
    fun `setLocalAudioMuted updates mediaStreamManager`() = runTest(testDispatcher) {
        val session = withSession()

        session.setLocalAudioMuted(true)
        assertThat(mediaStreamManager.isLocalAudioMuted).isTrue()

        session.setLocalAudioMuted(false)
        assertThat(mediaStreamManager.isLocalAudioMuted).isFalse()
    }

    /**
     * Helper function that creates a session, starts it, advances the dispatcher, executes the test body, and ensures
     * proper cleanup by cancelling the session job.
     */
    private suspend fun TestScope.withRunningSession(
        sessionId: String = "test-session-id",
        testBody: suspend TestScope.(Session) -> Unit,
    ) {
        val session = withSession(sessionId = sessionId)
        val job = launch {
            session.start()
        }

        runCurrent()

        try {
            testBody(session)
        } finally {
            job.cancel()
        }
    }

    private fun withSession(sessionId: String = "test-session-id") = Session(
        id = sessionId,
        signallingClient = signallingClient,
        streamingClient = streamingClient,
        mediaStreamManager = mediaStreamManager,
        messagingClient = messagingClient,
        toolClient = toolClient,
        sessionManager = platformSessionManager,
        logger = logger,
        isLoggingEnabled = false,
    )

    private fun withMessage(
        id: String = "msg-1",
        content: String,
        role: MessageRole = MessageRole.Persona,
        endOfSpeech: Boolean = true,
        interrupted: Boolean = false,
        correlationId: String? = null,
        version: Int = 1,
    ) = Message(
        id = id,
        content = content,
        role = role,
        endOfSpeech = endOfSpeech,
        interrupted = interrupted,
        correlationId = correlationId,
        version = version,
    )
}
