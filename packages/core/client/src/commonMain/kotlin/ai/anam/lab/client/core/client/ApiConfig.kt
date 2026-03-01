package ai.anam.lab.client.core.client

import ai.anam.lab.Environment

/**
 * Centralized configuration for the API endpoint and timeouts used across the app and SDK.
 */
data class ApiConfig(
    val environment: Environment = Environment.Production,
    val requestTimeoutMs: Long = DEFAULT_REQUEST_TIMEOUT_MS,
    val uploadTimeoutMs: Long = DEFAULT_UPLOAD_TIMEOUT_MS,
) {
    companion object {
        const val DEFAULT_REQUEST_TIMEOUT_MS = 100_000L
        const val DEFAULT_UPLOAD_TIMEOUT_MS = 300_000L
    }
}
