package ai.anam.lab

/**
 * This interface defines all supported events that are associated with an active [Session].
 */
public sealed interface SessionEvent {

    /**
     * A direct connection between the application and the Anam Engine is being established.
     */
    public data object Connecting : SessionEvent

    /**
     * A direct connection between the application and the Anam Engine has been established.
     */
    public data object ConnectionEstablished : SessionEvent

    /**
     * The session has been initialised and all the backend components are ready. Useful for removing loading
     * indicators.
     */
    public data class SessionReady(val sessionId: String) : SessionEvent

    /**
     * The direct connection between the application and the Anam Engine has been closed. This event includes a [reason]
     * that can be used to determine if an error occurred or if the connection was closed normally.
     */
    public data class ConnectionClosed(val reason: ConnectionClosedReason) : SessionEvent

    /**
     * The first frames have started playing during video streaming.
     */
    public data object VideoPlayStarted : SessionEvent

    /**
     * The user’s input audio stream (microphone) has been initialized
     */
    public data object InputAudioStreamStarted : SessionEvent

    /**
     * The user interrupts a TalkMessageStream by speaking. This event includes the interrupted stream’s correlationId.
     */
    public data object TalkStreamInterrupted : SessionEvent

    /**
     * The AI persona invokes a client tool. Includes the tool name and arguments for handling UI actions like
     * navigation or modal displays.
     */
    public data class ToolCall(val event: ToolEvent) : SessionEvent
}

/**
 * The reason why the direct connection between the application and the Anam Engine has been closed.
 */
public sealed interface ConnectionClosedReason {

    /**
     * The direct connection between the application and the Anam Engine has been closed normally.
     */
    public data object Normal : ConnectionClosedReason

    /**
     * The direct connection between the application and the Anam Engine has been closed due to an error with
     * establishing the direct connection.
     */
    public data class SignallingClientConnectionFailure(val message: String, val cause: Throwable? = null) :
        ConnectionClosedReason

    /**
     * The direct connection between the application and the Anam Engine has been closed due to an error with
     * establishing the WebRTC connection.
     */
    public data class WebRtcFailure(val message: String, val cause: Throwable? = null) : ConnectionClosedReason

    /**
     * The Anam Engine has closed the connection.
     */
    public data class ServerConnectionClosed(val reason: String) : ConnectionClosedReason

    /**
     * When requesting access to the device's microphone, permission was not granted.
     */
    public data object MicrophonePermissionDenied : ConnectionClosedReason
}

/**
 * Represents the Tool that the AI Persona wishes to invoke,
 */
public data class ToolEvent(
    val eventUid: String,
    val sessionId: String,
    val eventName: String,
    val eventData: String,
    val timestamp: String,
    val timestampUserAction: String,
    val userActionCorrelationId: String,
)
