package ai.anam.lab.webrtc

import ai.anam.lab.SessionEvent
import ai.anam.lab.ToolCallCompletedPayload
import ai.anam.lab.ToolCallFailedPayload
import ai.anam.lab.ToolCallHandler
import ai.anam.lab.ToolCallStartedPayload
import ai.anam.lab.api.DataChannelMessage
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.fakes.FakeLogger
import ai.anam.lab.fakes.toolCallCompletedMessage
import ai.anam.lab.fakes.toolCallFailedMessage
import ai.anam.lab.fakes.toolCallStartedMessage
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class ToolCallClientTest {
    private val testDispatcher = StandardTestDispatcher()
    private val logger = FakeLogger()
    private val dataChannelMessages = MutableSharedFlow<DataChannelMessage>()
    private val toolCallClient = ToolCallClientImpl(dataChannelMessages, logger)

    // region Event Emission

    @Test
    fun `emits ToolCallStarted event for started message`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted()

                val event = awaitItem()
                assertThat(event).isInstanceOf<SessionEvent.ToolCallStarted>()

                val payload = (event as SessionEvent.ToolCallStarted).payload
                assertThat(payload.toolCallId).isEqualTo("tc-001")
                assertThat(payload.toolName).isEqualTo("redirect")
                assertThat(payload.toolType).isEqualTo("server")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `emits ToolCallCompleted event for completed message`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallCompleted()

                val event = awaitItem()
                assertThat(event).isInstanceOf<SessionEvent.ToolCallCompleted>()

                val payload = (event as SessionEvent.ToolCallCompleted).payload
                assertThat(payload.toolCallId).isEqualTo("tc-001")
                assertThat(payload.result).isEqualTo("success")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `emits ToolCallFailed event for failed message`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallFailed()

                val event = awaitItem()
                assertThat(event).isInstanceOf<SessionEvent.ToolCallFailed>()

                val payload = (event as SessionEvent.ToolCallFailed).payload
                assertThat(payload.toolCallId).isEqualTo("tc-001")
                assertThat(payload.errorMessage).isEqualTo("timeout")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // endregion

    // region Execution Time

    @Test
    fun `calculates executionTime between started and completed`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted()
                awaitItem() // ToolCallStarted

                emitToolCallCompleted()
                val event = awaitItem() as SessionEvent.ToolCallCompleted

                assertThat(event.payload.executionTime).isNotNull()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `executionTime is null when completed arrives without prior started`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallCompleted(toolCallId = "unknown-tc")

                val event = awaitItem() as SessionEvent.ToolCallCompleted
                assertThat(event.payload.executionTime).isNull()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `calculates executionTime between started and failed`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted()
                awaitItem() // ToolCallStarted

                emitToolCallFailed()
                val event = awaitItem() as SessionEvent.ToolCallFailed

                assertThat(event.payload.executionTime).isNotNull()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // endregion

    // region Handler Invocation

    @Test
    fun `registerHandler invokes onStart callback for matching tool name`() = runTest(testDispatcher) {
        var receivedPayload: ToolCallStartedPayload? = null
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = { payload ->
                    receivedPayload = payload
                    null
                },
            ),
        )

        withProcessing {
            emitToolCallStarted()
            runCurrent()

            assertThat(receivedPayload).isNotNull()
            assertThat(receivedPayload!!.toolName).isEqualTo("redirect")
        }
    }

    @Test
    fun `registerHandler invokes onComplete callback for matching tool name`() = runTest(testDispatcher) {
        var receivedPayload: ToolCallCompletedPayload? = null
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onComplete = { payload -> receivedPayload = payload },
            ),
        )

        withProcessing {
            emitToolCallCompleted()
            runCurrent()

            assertThat(receivedPayload).isNotNull()
            assertThat(receivedPayload!!.toolName).isEqualTo("redirect")
        }
    }

    @Test
    fun `registerHandler invokes onFail callback for matching tool name`() = runTest(testDispatcher) {
        var receivedPayload: ToolCallFailedPayload? = null
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onFail = { payload -> receivedPayload = payload },
            ),
        )

        withProcessing {
            emitToolCallFailed()
            runCurrent()

            assertThat(receivedPayload).isNotNull()
            assertThat(receivedPayload!!.errorMessage).isEqualTo("timeout")
        }
    }

    @Test
    fun `handler is not invoked for non-matching tool name`() = runTest(testDispatcher) {
        var invoked = false
        toolCallClient.registerHandler(
            "other-tool",
            ToolCallHandler(
                onStart = {
                    invoked = true
                    null
                },
            ),
        )

        withProcessing {
            emitToolCallStarted(toolName = "redirect")
            runCurrent()

            assertThat(invoked).isFalse()
        }
    }

    @Test
    fun `unregister function removes handler`() = runTest(testDispatcher) {
        var invoked = false
        val unregister = toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = {
                    invoked = true
                    null
                },
            ),
        )

        unregister()

        withProcessing {
            emitToolCallStarted()
            runCurrent()

            assertThat(invoked).isFalse()
        }
    }

    // endregion

    // region Client Tool Auto-Complete

    @Test
    fun `client tool onStart return value triggers completed event`() = runTest(testDispatcher) {
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = { "done" },
            ),
        )

        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted(toolType = "client")

                val startedEvent = awaitItem()
                assertThat(startedEvent).isInstanceOf<SessionEvent.ToolCallStarted>()

                val completedEvent = awaitItem()
                assertThat(completedEvent).isInstanceOf<SessionEvent.ToolCallCompleted>()

                val payload = (completedEvent as SessionEvent.ToolCallCompleted).payload
                assertThat(payload.result).isEqualTo("done")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `client tool does not auto-complete when onStart is null`() = runTest(testDispatcher) {
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = null,
                onComplete = { },
            ),
        )

        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted(toolType = "client")

                val startedEvent = awaitItem()
                assertThat(startedEvent).isInstanceOf<SessionEvent.ToolCallStarted>()

                // No auto-complete event should follow — only the started event.
                expectNoEvents()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `client tool auto-complete invokes onComplete callback`() = runTest(testDispatcher) {
        var receivedPayload: ToolCallCompletedPayload? = null
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = { "done" },
                onComplete = { payload -> receivedPayload = payload },
            ),
        )

        withProcessing {
            emitToolCallStarted(toolType = "client")
            runCurrent()

            assertThat(receivedPayload).isNotNull()
            assertThat(receivedPayload!!.result).isEqualTo("done")
        }
    }

    @Test
    fun `client tool onStart exception triggers failed event`() = runTest(testDispatcher) {
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = { throw RuntimeException("handler error") },
            ),
        )

        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted(toolType = "client")

                val startedEvent = awaitItem()
                assertThat(startedEvent).isInstanceOf<SessionEvent.ToolCallStarted>()

                val failedEvent = awaitItem()
                assertThat(failedEvent).isInstanceOf<SessionEvent.ToolCallFailed>()

                val payload = (failedEvent as SessionEvent.ToolCallFailed).payload
                assertThat(payload.errorMessage).isEqualTo("handler error")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `client tool auto-fail invokes onFail callback`() = runTest(testDispatcher) {
        var receivedPayload: ToolCallFailedPayload? = null
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = { throw RuntimeException("handler error") },
                onFail = { payload -> receivedPayload = payload },
            ),
        )

        withProcessing {
            emitToolCallStarted(toolType = "client")
            runCurrent()

            assertThat(receivedPayload).isNotNull()
            assertThat(receivedPayload!!.errorMessage).isEqualTo("handler error")
        }
    }

    // endregion

    // region Error Handling

    @Test
    fun `handler errors do not crash the processing loop`() = runTest(testDispatcher) {
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onComplete = { throw RuntimeException("callback error") },
            ),
        )

        withProcessing {
            toolCallClient.events.test {
                // First message — handler throws but processing continues.
                emitToolCallCompleted()
                awaitItem()

                // Second message — should still be processed.
                emitToolCallCompleted(toolCallId = "tc-002")
                val event = awaitItem() as SessionEvent.ToolCallCompleted
                assertThat(event.payload.toolCallId).isEqualTo("tc-002")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // endregion

    // region Release

    @Test
    fun `release resets tracked pending calls`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                emitToolCallStarted()
                awaitItem() // Consume started

                toolCallClient.release()

                emitToolCallCompleted()
                val event = awaitItem() as SessionEvent.ToolCallCompleted
                assertThat(event.payload.executionTime).isNull()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun `release clears registered handlers`() = runTest(testDispatcher) {
        var invoked = false
        toolCallClient.registerHandler(
            "redirect",
            ToolCallHandler(
                onStart = {
                    invoked = true
                    null
                },
            ),
        )

        toolCallClient.release()

        withProcessing {
            emitToolCallStarted()
            runCurrent()

            assertThat(invoked).isFalse()
        }
    }

    // endregion

    // region Filtering

    @Test
    fun `non-tool-call messages are ignored`() = runTest(testDispatcher) {
        withProcessing {
            toolCallClient.events.test {
                // Emit a text message — should not produce any event
                dataChannelMessages.emit(
                    DataChannelMessage(
                        type = DataChannelMessageType.SpeechText,
                        data = DataChannelMessagePayload.TextMessage(
                            id = "msg-1",
                            index = 0,
                            content = "Hello",
                            role = "persona",
                            endOfSpeech = true,
                            interrupted = false,
                        ),
                    ),
                )

                // Then emit a tool call — should produce event
                emitToolCallStarted()
                val event = awaitItem()
                assertThat(event).isInstanceOf<SessionEvent.ToolCallStarted>()

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    // endregion

    private suspend fun TestScope.withProcessing(block: suspend TestScope.() -> Unit) {
        val job = launch { toolCallClient.processMessages() }
        runCurrent()
        try {
            block()
        } finally {
            job.cancel()
        }
    }

    // Defaults to "server" toolType (overriding the fixture's "client" default) because most
    // ToolCallClient tests verify the server-tool path. Client-tool tests pass toolType explicitly.
    private suspend fun emitToolCallStarted(
        toolCallId: String = "tc-001",
        toolName: String = "redirect",
        toolType: String = "server",
    ) {
        dataChannelMessages.emit(
            DataChannelMessage(
                type = DataChannelMessageType.ToolCallStarted,
                data = toolCallStartedMessage(
                    toolCallId = toolCallId,
                    toolName = toolName,
                    toolType = toolType,
                ),
            ),
        )
    }

    private suspend fun emitToolCallCompleted(toolCallId: String = "tc-001") {
        dataChannelMessages.emit(
            DataChannelMessage(
                type = DataChannelMessageType.ToolCallCompleted,
                data = toolCallCompletedMessage(toolCallId = toolCallId),
            ),
        )
    }

    private suspend fun emitToolCallFailed(toolCallId: String = "tc-001") {
        dataChannelMessages.emit(
            DataChannelMessage(
                type = DataChannelMessageType.ToolCallFailed,
                data = toolCallFailedMessage(toolCallId = toolCallId, errorMessage = "timeout"),
            ),
        )
    }
}
