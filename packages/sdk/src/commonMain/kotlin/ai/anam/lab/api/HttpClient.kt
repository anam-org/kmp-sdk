package ai.anam.lab.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * The default [Json] configuration used by ktor.
 */
internal val defaultJsonConfiguration = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

/**
 * Builds a default [HttpClient] that uses [BearerTokens] for authentication. This is ideally the session token that
 * has been created server side, but for testing purposes, it's also possible to specify the API key directly. However,
 * this latter option should not be used in production.
 */
internal fun buildApiHttpClient(token: String) = HttpClient {
    install(Auth) {
        bearer {
            loadTokens {
                BearerTokens(token, null)
            }
        }
    }

    install(ContentNegotiation) {
        json(defaultJsonConfiguration)
    }

    // Configure ktor to throw a ResponseException if we don't get a successful status code.
    expectSuccess = true
}

internal fun buildWebSocketClient() = HttpClient {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(defaultJsonConfiguration)
    }
}
