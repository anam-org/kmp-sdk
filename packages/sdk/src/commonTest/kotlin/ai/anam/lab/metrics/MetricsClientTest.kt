package ai.anam.lab.metrics

import ai.anam.lab.AnamClientMetadata
import ai.anam.lab.ApiGateway
import ai.anam.lab.api.defaultJsonConfiguration
import ai.anam.lab.fakes.FakeLogger
import assertk.assertThat
import assertk.assertions.containsAtLeast
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class MetricsClientTest {

    private val logger = FakeLogger()
    private val json = defaultJsonConfiguration

    @Test
    fun `sends metric to correct URL with correct JSON body`() = runTest {
        var capturedUrl = ""
        var capturedBody = ""

        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString()
            capturedBody = request.body.toByteArray().decodeToString()
            respond(content = "", status = HttpStatusCode.OK)
        }

        val client = createMetricsClient(mockEngine)
        client.send(ClientMetric.SessionAttempt)

        assertThat(capturedUrl).isEqualTo("https://api.example.com/v1/metrics/client")

        val body = json.decodeFromString<ClientMetricBody>(capturedBody)
        assertThat(body.name).isEqualTo("client_session_attempt")
        assertThat(body.value).isEqualTo("1")
    }

    @Test
    fun `includes client metadata tags`() = runTest {
        var capturedBody = ""

        val mockEngine = MockEngine { request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(content = "", status = HttpStatusCode.OK)
        }

        val client = createMetricsClient(mockEngine)
        client.send(ClientMetric.SessionAttempt)

        val body = json.decodeFromString<ClientMetricBody>(capturedBody)
        assertThat(body.tags).containsAtLeast(
            "client" to AnamClientMetadata.getName(),
            "version" to AnamClientMetadata.getVersion(),
        )
    }

    @Test
    fun `includes session context tags after updateContext`() = runTest {
        var capturedBody = ""

        val mockEngine = MockEngine { request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(content = "", status = HttpStatusCode.OK)
        }

        val client = createMetricsClient(mockEngine)
        client.updateContext(MetricsContext(sessionId = "session-123", organizationId = "org-456"))
        client.send(ClientMetric.ConnectionEstablished)

        val body = json.decodeFromString<ClientMetricBody>(capturedBody)
        assertThat(body.tags).containsAtLeast(
            "session_id" to "session-123",
            "organization_id" to "org-456",
        )
    }

    @Test
    fun `includes caller-provided tags`() = runTest {
        var capturedBody = ""

        val mockEngine = MockEngine { request ->
            capturedBody = request.body.toByteArray().decodeToString()
            respond(content = "", status = HttpStatusCode.OK)
        }

        val client = createMetricsClient(mockEngine)
        client.send(ClientMetric.Error, tags = mapOf("error" to "something went wrong"))

        val body = json.decodeFromString<ClientMetricBody>(capturedBody)
        assertThat(body.tags).containsAtLeast("error" to "something went wrong")
    }

    @Test
    fun `routes through API gateway when configured`() = runTest {
        var capturedUrl = ""
        var capturedTargetHeader = ""

        val mockEngine = MockEngine { request ->
            capturedUrl = request.url.toString()
            capturedTargetHeader = request.headers["X-Anam-Target-Url"] ?: ""
            respond(content = "", status = HttpStatusCode.OK)
        }

        val client = createMetricsClient(
            mockEngine = mockEngine,
            apiGateway = ApiGateway(baseUrl = "https://gateway.example.com"),
        )
        client.send(ClientMetric.SessionAttempt)

        assertThat(capturedUrl).isEqualTo("https://gateway.example.com/v1/metrics/client")
        assertThat(capturedTargetHeader).isEqualTo("https://api.example.com/v1/metrics/client")
    }

    @Test
    fun `HTTP errors do not propagate`() = runTest {
        val mockEngine = MockEngine {
            respond(content = "", status = HttpStatusCode.InternalServerError)
        }

        val client = createMetricsClient(mockEngine)

        // Should not throw
        client.send(ClientMetric.SessionAttempt)

        // Verify the request was made (engine received it)
        assertThat(mockEngine.requestHistory).isNotEmpty()
    }

    @Test
    fun `context update reflects in subsequent sends`() = runTest {
        val bodies = mutableListOf<String>()

        val mockEngine = MockEngine { request ->
            bodies.add(request.body.toByteArray().decodeToString())
            respond(content = "", status = HttpStatusCode.OK)
        }

        val client = createMetricsClient(mockEngine)

        // Send without context
        client.send(ClientMetric.SessionAttempt)

        // Update context and send again
        client.updateContext(MetricsContext(sessionId = "session-789"))
        client.send(ClientMetric.SessionSuccess)

        val firstBody = json.decodeFromString<ClientMetricBody>(bodies[0])
        assertThat(firstBody.tags.containsKey("session_id")).isEqualTo(false)

        val secondBody = json.decodeFromString<ClientMetricBody>(bodies[1])
        assertThat(secondBody.tags).containsAtLeast("session_id" to "session-789")
    }

    @Test
    fun `NoOpMetricsClient send does not throw`() = runTest {
        NoOpMetricsClient.send(ClientMetric.SessionAttempt)
        NoOpMetricsClient.send(ClientMetric.Error, tags = mapOf("error" to "test"))
        NoOpMetricsClient.updateContext(MetricsContext(sessionId = "ignored"))
    }

    private fun createMetricsClient(
        mockEngine: MockEngine,
        baseUrl: String = "https://api.example.com/",
        apiGateway: ApiGateway? = null,
    ): MetricsClientImpl {
        val httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
        }
        return MetricsClientImpl(
            baseUrl = baseUrl,
            apiGateway = apiGateway,
            httpClient = httpClient,
            logger = logger,
        )
    }
}
