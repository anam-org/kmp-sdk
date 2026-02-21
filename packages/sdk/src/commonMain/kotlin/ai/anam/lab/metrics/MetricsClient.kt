package ai.anam.lab.metrics

import ai.anam.lab.AnamClientMetadata
import ai.anam.lab.ApiGateway
import ai.anam.lab.utils.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.serialization.Serializable

/**
 * Sends client-side metrics to the Anam metrics API. Calls to [send] are fire-and-forget; failures are logged and
 * never propagated to the caller.
 */
internal interface MetricsClient {
    suspend fun send(metric: ClientMetric, value: String = "1", tags: Map<String, String> = emptyMap())
    fun updateContext(context: MetricsContext)

    /** Releases the underlying HTTP resources. No further calls to [send] should be made after this. */
    fun close()
}

@OptIn(ExperimentalAtomicApi::class)
internal class MetricsClientImpl(
    private val baseUrl: String,
    private val apiGateway: ApiGateway?,
    private val httpClient: HttpClient,
    private val logger: Logger,
) : MetricsClient {
    private val context = AtomicReference(MetricsContext())

    override suspend fun send(metric: ClientMetric, value: String, tags: Map<String, String>) {
        try {
            val currentContext = context.load()
            val allTags = buildMap {
                put(ClientTags.CLIENT, AnamClientMetadata.getName())
                put(ClientTags.VERSION, AnamClientMetadata.getVersion())
                currentContext.sessionId?.let { put(ClientTags.SESSION_ID, it) }
                currentContext.organizationId?.let { put(ClientTags.ORGANIZATION_ID, it) }
                currentContext.attemptCorrelationId?.let { put(ClientTags.ATTEMPT_CORRELATION_ID, it) }
                putAll(tags)
            }

            val body = ClientMetricBody(
                name = metric.metricName,
                value = value,
                tags = allTags,
            )

            // The actual metrics endpoint on the Anam API.
            val targetUrl = buildMetricsUrl(baseUrl)

            // When an API gateway is configured, route through it instead; otherwise hit the target directly.
            val requestUrl = apiGateway?.let { buildMetricsUrl(it.baseUrl) } ?: targetUrl

            httpClient.post(requestUrl) {
                contentType(ContentType.Application.Json)
                if (apiGateway != null) header("X-Anam-Target-Url", targetUrl)
                setBody(body)
            }
        } catch (e: Exception) {
            logger.d(TAG) { "Failed to send metric ${metric.metricName}: ${e.message}" }
        }
    }

    override fun updateContext(context: MetricsContext) {
        logger.d(TAG) { "Updating metrics context: $context" }
        this.context.store(context)
    }

    override fun close() {
        httpClient.close()
    }

    private companion object {
        const val TAG = "MetricsClient"
        const val METRICS_PATH = "v1/metrics/client"

        fun buildMetricsUrl(base: String) = URLBuilder(base).appendPathSegments(METRICS_PATH).buildString()
    }
}

internal data object NoOpMetricsClient : MetricsClient {
    override suspend fun send(metric: ClientMetric, value: String, tags: Map<String, String>) = Unit
    override fun updateContext(context: MetricsContext) = Unit
    override fun close() = Unit
}

/**
 * The POST body sent to the Anam metrics API.
 */
@Serializable
internal data class ClientMetricBody(val name: String, val value: String, val tags: Map<String, String>)
