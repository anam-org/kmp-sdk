package ai.anam.lab.client.domain.data

/**
 * Returns the currently configured API key, or `null` if no key is set.
 */
fun interface GetApiKeyInteractor {
    operator fun invoke(): String?
}
