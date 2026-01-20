package ai.anam.lab.client.core.auth

import dev.zacsweers.metro.Inject

@Inject
class AuthRepository {
    /**
     * For now, we're just getting a hardcoded API token from the build. This needs to be generated via the Anam Labs
     * application and associated with your account.
     */
    fun getApiToken() = BuildConfig.API_TOKEN.ifEmpty { null }
}
