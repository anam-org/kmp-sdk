package ai.anam.lab.client.domain.data

/**
 * Persists a new API key and resets the active persona if the key changed.
 * Returns `true` if the key was different from the previous value.
 */
fun interface UpdateApiKeyInteractor {
    suspend operator fun invoke(apiKey: String): Boolean
}
