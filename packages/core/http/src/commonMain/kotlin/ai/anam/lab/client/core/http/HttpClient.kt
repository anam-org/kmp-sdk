package ai.anam.lab.client.core.http

import ai.anam.lab.client.core.logging.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The default [Json] configuration used by ktor.
 */
val defaultJsonConfiguration = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

/**
 * Function to build a [HttpClient] that can talk to the Anam API. This API uses a physical key, rather than a session
 * token.
 */
fun buildApiHttpClient(token: () -> String?, logger: Logger) = HttpClient {
    install(Auth) {
        bearer {
            loadTokens {
                val current = token()
                return@loadTokens if (current != null) {
                    BearerTokens(current, null)
                } else {
                    logger.e(TAG) { "No API Token Found" }
                    null
                }
            }
        }
    }

    install(ContentNegotiation) {
        json(defaultJsonConfiguration)
    }

    // Configure ktor to throw a ResponseException if we don't get a successful status code.
    expectSuccess = true
}

private const val TAG = "HttpClient"
