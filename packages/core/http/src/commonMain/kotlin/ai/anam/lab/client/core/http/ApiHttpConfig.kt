package ai.anam.lab.client.core.http

/**
 * API configuration bridge derived from the ApiConfig in the core/client module. Carries the base URL and timeout
 * values so that core/http and downstream modules (e.g. core/data) do not need a dependency on the SDK.
 */
data class ApiHttpConfig(val baseUrl: String, val requestTimeoutMs: Long, val uploadTimeoutMs: Long)
