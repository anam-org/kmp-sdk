package ai.anam.lab.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface SessionTokenApi {
    @POST("v1/auth/session-token")
    @Headers("Content-Type: application/json")
    suspend fun getSessionToken(@Body data: SessionTokenBody): SessionTokenResponse
}

@Serializable
internal data class SessionTokenBody(
    @SerialName("personaConfig")
    val personaConfig: PersonaConfig,
)

@Serializable
internal data class PersonaConfig(
    @SerialName("name")
    val name: String,
    @SerialName("avatarId")
    val avatarId: String,
    @SerialName("voiceId")
    val voiceId: String,
    @SerialName("llmId")
    val llmId: String?,
    @SerialName("systemPrompt")
    val systemPrompt: String,
    @SerialName("maxSessionLengthSeconds")
    val maxSessionLengthSeconds: Int,
)

@Serializable
internal data class SessionTokenResponse(
    @SerialName("sessionToken")
    val sessionToken: String,
)
