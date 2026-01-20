package ai.anam.lab.webrtc

import ai.anam.lab.ApiGateway
import ai.anam.lab.ConnectionClosedReason
import ai.anam.lab.SessionEvent
import ai.anam.lab.api.ClientConfig
import ai.anam.lab.api.SessionConfig
import ai.anam.lab.fakes.FakeLogger
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isTrue
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class SignallingClientTest {
    private val testDispatcher = StandardTestDispatcher()

    private var lastRequestUrl: String? = null
    private var requestCount: Int = 0
    private val logger = FakeLogger()

    // region URL Building Tests

    @Test
    fun `webSocket is called with correct URL`() = runTest(testDispatcher) {
        val config = createSessionConfig(
            engineHost = "test.example.com/custom-ws",
            engineProtocol = "https",
            signallingEndpoint = "/custom-ws",
        )

        val client = withClient(config)
        connectAndCaptureUrl(client)

        assertThat(lastRequestUrl).isNotNull()
        assertThat(lastRequestUrl).isEqualTo("wss://test.example.com/custom-ws?session_id=test-session-id")
    }

    @Test
    fun `webSocket URL uses HTTP protocol for HTTP engine protocol`() = runTest(testDispatcher) {
        val config = createSessionConfig(
            engineProtocol = "http",
        )

        val client = withClient(config)
        connectAndCaptureUrl(client)

        assertThat(lastRequestUrl).isNotNull()
        assertThat(lastRequestUrl).isEqualTo("ws://test.example.com/ws?session_id=test-session-id")
    }

    @Test
    fun `webSocket URL uses default path when signallingEndpoint is null`() = runTest(testDispatcher) {
        val config = createSessionConfig()

        val client = withClient(config)
        connectAndCaptureUrl(client)

        assertThat(lastRequestUrl).isNotNull()
        assertThat(lastRequestUrl).isEqualTo("wss://test.example.com/ws?session_id=test-session-id")
    }

    @Test
    fun `webSocket URL includes API gateway when provided`() = runTest(testDispatcher) {
        val config = createSessionConfig(
            signallingEndpoint = "/custom-ws",
        )
        val apiGateway = createApiGateway(
            baseUrl = "https://gateway.example.com",
            wsPath = "/api/ws",
        )

        val client = withClient(config, apiGateway)
        connectAndCaptureUrl(client)

        assertThat(lastRequestUrl).isNotNull()
        val url = lastRequestUrl!!
        assertThat(url.contains("https://gateway.example.com/api/ws")).isTrue()
        assertThat(
            url.contains("target_url=wss%3A%2F%2Ftest.example.com%2Fcustom-ws%3Fsession_id%3Dtest-session-id"),
        ).isTrue()
    }

    @Test
    fun `webSocket URL uses default gateway path when wsPath is null`() = runTest(testDispatcher) {
        val config = createSessionConfig()
        val apiGateway = createApiGateway(
            baseUrl = "https://gateway.example.com",
            wsPath = null,
        )

        val client = withClient(config, apiGateway)
        connectAndCaptureUrl(client)

        assertThat(lastRequestUrl).isNotNull()
        assertThat(lastRequestUrl!!.contains("https://gateway.example.com/ws")).isTrue()
    }

    // endregion

    // region Connection State and Flow Tests

    @Test
    fun `webSocket retries to connect on error`() = runTest(testDispatcher) {
        val attempts = 2
        val config = createSessionConfig(reconnectAttempts = attempts)
        val client = withClient(config)

        connectAndWait(client)

        assertThat(requestCount).isEqualTo(attempts + 1)
    }

    @Test
    fun `connected flow emits false when connection fails`() = runTest(testDispatcher) {
        val config = createSessionConfig(reconnectAttempts = 0)
        val client = withClient(config)

        val job = connectAndWait(client)

        client.connected.test {
            assertThat(awaitItem()).isFalse()
        }

        job.cancel()
    }

    @Test
    fun `client signals when connection is closed`() = runTest(testDispatcher) {
        val config = createSessionConfig()
        val client = withClient(config)
        val job = launch { client.connect() }

        client.events.test {
            awaitItem().let {
                assertThat(it).isInstanceOf<SessionEvent.ConnectionClosed>()

                val event = it as SessionEvent.ConnectionClosed
                assertThat(event.reason).isInstanceOf<ConnectionClosedReason.SignallingClientConnectionFailure>()
            }
        }

        job.cancel()
    }

    // endregion

    // region Helper Functions

    private fun createSessionConfig(
        sessionId: String = "test-session-id",
        engineHost: String = "test.example.com",
        engineProtocol: String = "https",
        signallingEndpoint: String? = null,
        reconnectAttempts: Int = 0,
    ) = SessionConfig(
        sessionId = sessionId,
        engineHost = engineHost,
        engineProtocol = engineProtocol,
        signallingEndpoint = signallingEndpoint,
        clientConfig = ClientConfig(
            maxWsReconnectAttempts = reconnectAttempts,
            iceServers = emptyList(),
        ),
    )

    private fun createApiGateway(baseUrl: String, wsPath: String? = null) = ApiGateway(
        baseUrl = baseUrl,
        wsPath = wsPath,
    )

    private fun withClient(config: SessionConfig, apiGateway: ApiGateway? = null) = SignallingClientImpl(
        config = config,
        apiGateway = apiGateway,
        httpClient = withHttpClient(),
        logger = logger,
        ioDispatcher = testDispatcher,
    )

    private fun TestScope.connectAndCaptureUrl(client: SignallingClientImpl) {
        val job = launch { client.connect() }
        runCurrent()
        job.cancel()
    }

    private fun TestScope.connectAndWait(client: SignallingClientImpl): Job {
        val job = launch { client.connect() }
        advanceUntilIdle()
        return job
    }

    private fun withHttpClient(): HttpClient {
        // Create a MockEngine that captures the URL and uses the test dispatcher
        val mockHttpClient = HttpClient(MockEngine) {
            engine {
                dispatcher = testDispatcher
                addHandler { request ->
                    lastRequestUrl = request.url.toString()
                    requestCount++
                    respondError(HttpStatusCode.BadRequest)
                }
            }
            install(WebSockets)
        }

        return mockHttpClient
    }

    // endregion
}
