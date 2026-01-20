package ai.anam.lab

import ai.anam.lab.AnamClient.Companion.TAG
import ai.anam.lab.api.ApiResult
import ai.anam.lab.api.PersonaConfig as ApiPersonaConfig
import ai.anam.lab.api.SessionTokenBody
import ai.anam.lab.api.SessionTokenResponse
import ai.anam.lab.api.apiCall
import ai.anam.lab.api.buildApiHttpClient
import ai.anam.lab.api.cause
import ai.anam.lab.api.createSessionTokenApi
import ai.anam.lab.api.message
import ai.anam.lab.utils.UnsafeAnamApi
import de.jensklingenberg.ktorfit.Ktorfit

/**
 * Create a new Anam [Session] with an API key instead of a session token.
 *
 * This method is unsafe for production environments because it requires exposing your API key to the client side. Only
 * use this method for local testing.
 */
@UnsafeAnamApi
public suspend fun AnamClient.createUnsafeSession(
    apiKey: String,
    personaConfig: PersonaConfig,
    sessionOptions: SessionOptions = SessionOptions(),
): SessionResult {
    logger.w(TAG) { "Starting unsafe session with API key..." }

    // Since the caller has provided us their API key, we need to use it for Bearer Authentication when making network
    // requests. This will only be used to create the initial session token, and then we'll fallback to using that
    // token for all subsequent requests.
    val httpClient = buildApiHttpClient(apiKey)
    val ktorfit = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl(options.environment.baseUrl)
        .build()

    // Create the initial session token.
    val tokenApi = ktorfit.createSessionTokenApi()
    val tokenResult = apiCall {
        tokenApi.getSessionToken(
            SessionTokenBody(
                personaConfig = personaConfig.toApiPersonaConfig(),
            ),
        )
    }

    return when (tokenResult) {
        is ApiResult.Success<SessionTokenResponse> -> {
            return createSession(tokenResult.data.sessionToken, sessionOptions)
        }

        is ApiResult.Error -> {
            logger.e(TAG) { "Failed to get session token: $tokenResult" }
            SessionResult.Error(
                message = tokenResult.message,
                cause = tokenResult.cause,
            )
        }
    }
}

/**
 * This class represents the configuration for the Persona when creating a local (unsafe!) session token.
 */
@UnsafeAnamApi
public data class PersonaConfig(
    val name: String,
    val avatarId: String,
    val voiceId: String,
    val llmId: String?,
    val systemPrompt: String,
)

/**
 * Extension function to convert from our public model, exposed to consumers, to our own internal API model.
 */
private fun PersonaConfig.toApiPersonaConfig() = ApiPersonaConfig(
    name = name,
    avatarId = avatarId,
    voiceId = voiceId,
    llmId = llmId,
    systemPrompt = systemPrompt,
)
