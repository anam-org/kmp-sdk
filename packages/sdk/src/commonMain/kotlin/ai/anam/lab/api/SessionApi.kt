package ai.anam.lab.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal interface SessionApi {
    @POST("v1/engine/session")
    @Headers("Content-Type: application/json")
    suspend fun startSession(@Body options: SessionBody): SessionConfig
}

@Serializable
internal data class SessionBody(
    @SerialName("clientMetadata")
    val clientMetadata: ClientMetadata,

    @SerialName("voiceDetection")
    val voiceDetection: VoiceDetectionOptions? = null,
)

@Serializable
internal data class ClientMetadata(
    @SerialName("client")
    val client: String,

    @SerialName("version")
    val version: String,
)

@Serializable
internal data class VoiceDetectionOptions(
    @SerialName("endOfSpeechSensitivity")
    val endOfSpeechSensitivity: Int? = null,
)

@Serializable
internal data class SessionConfig(
    @SerialName("sessionId")
    val sessionId: String,

    @SerialName("engineHost")
    val engineHost: String,

    @SerialName("engineProtocol")
    val engineProtocol: String,

    @SerialName("signallingEndpoint")
    val signallingEndpoint: String? = null,

    @SerialName("clientConfig")
    val clientConfig: ClientConfig,
)

@Serializable
internal data class ClientConfig(
    @SerialName("expectedHeartbeatIntervalSecs")
    val expectedHeartbeatIntervalSecs: Int? = null,

    @SerialName("maxWsReconnectAttempts")
    val maxWsReconnectAttempts: Int? = null,

    @SerialName("iceServers")
    val iceServers: List<RTCIceServer>,
)

@Serializable
internal data class RTCIceServer(
    @SerialName("urls")
    val urls: List<String>,

    @SerialName("username")
    val username: String? = null,

    @SerialName("credential")
    val credential: String? = null,

    @SerialName("credentialType")
    val credentialType: RTCIceCredentialType,
)

@Serializable
internal enum class RTCIceCredentialType {
    @SerialName("password")
    Password,

    @SerialName("oauth")
    OAuth,
}
