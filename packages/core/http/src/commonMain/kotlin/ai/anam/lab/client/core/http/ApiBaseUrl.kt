package ai.anam.lab.client.core.http

/**
 * Typed wrapper around the API base URL string, used for dependency injection. The actual value is derived from the
 * ApiConfig defined in the core/client module.
 */
data class ApiBaseUrl(val value: String)
