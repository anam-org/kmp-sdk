package ai.anam.lab.fakes

import ai.anam.lab.metrics.ClientMetric
import ai.anam.lab.metrics.MetricsClient
import ai.anam.lab.metrics.MetricsContext

internal class FakeMetricsClient : MetricsClient {

    data class SentMetric(val metric: ClientMetric, val value: String, val tags: Map<String, String>)

    val sentMetrics = mutableListOf<SentMetric>()
    var currentContext: MetricsContext? = null

    override suspend fun send(metric: ClientMetric, value: String, tags: Map<String, String>) {
        sentMetrics.add(SentMetric(metric, value, tags))
    }

    override fun updateContext(context: MetricsContext) {
        currentContext = context
    }

    override fun close() = Unit
}
