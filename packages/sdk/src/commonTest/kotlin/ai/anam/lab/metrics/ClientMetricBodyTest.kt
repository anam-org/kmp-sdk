package ai.anam.lab.metrics

import ai.anam.lab.api.defaultJsonConfiguration
import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class ClientMetricBodyTest {

    private val json = defaultJsonConfiguration

    @Test
    fun `serialization round-trip preserves all fields`() {
        val body = ClientMetricBody(
            name = "client_session_attempt",
            value = "1",
            tags = mapOf("client" to "kmp-sdk", "session_id" to "abc-123"),
        )

        val encoded = json.encodeToString(ClientMetricBody.serializer(), body)
        val decoded = json.decodeFromString<ClientMetricBody>(encoded)

        assertThat(decoded).isEqualTo(body)
    }

    @Test
    fun `serialization produces expected JSON structure`() {
        val body = ClientMetricBody(
            name = "client_error",
            value = "1",
            tags = mapOf("error" to "timeout"),
        )

        val encoded = json.encodeToString(ClientMetricBody.serializer(), body)
        val decoded = json.decodeFromString<ClientMetricBody>(encoded)

        assertThat(decoded.name).isEqualTo("client_error")
        assertThat(decoded.value).isEqualTo("1")
        assertThat(decoded.tags).isEqualTo(mapOf("error" to "timeout"))
    }
}
