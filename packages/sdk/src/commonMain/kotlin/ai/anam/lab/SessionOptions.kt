package ai.anam.lab

/**
 * Configuration when creating a new Session.
 */
public data class SessionOptions(
    /**
     * Configuration for routing SDK requests through an API Gateway.
     *
     * When enabled, the SDK will route HTTP REST calls and WebSocket signalling through the specified API Gateway while
     * maintaining direct WebRTC peer connections.
     */
    val apiGateway: ApiGateway? = null,

    /**
     * If enabled, local audio will be captured and sent. This requires the necessary permission to use the device's
     * microphone which is the responsibility of the hosting application.
     */
    val isLocalAudioEnabled: Boolean = true,

    /**
     * Configuration for voice detection and the sensitivity when detecting the end of speech.
     */
    val endOfSpeechSensitivity: Int? = null,

    /**
     * When `true`, client-side metrics collection is disabled. Metrics are enabled by default.
     */
    val isMetricsDisabled: Boolean = false,
)
