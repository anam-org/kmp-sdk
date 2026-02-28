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
     * A tool call has been started by the AI Persona.
     */
    public data class ToolCallStarted(val payload: ToolCallStartedPayload) : SessionEvent

    /**
     * A tool call has completed successfully.
     */
    public data class ToolCallCompleted(val payload: ToolCallCompletedPayload) : SessionEvent

    /**
     * A tool call has failed.
     */
    public data class ToolCallFailed(val payload: ToolCallFailedPayload) : SessionEvent
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
 * Payload for a tool call that has been started by the AI Persona.
 */
public data class ToolCallStartedPayload(
    val eventUid: String,
    val toolCallId: String,
    val toolName: String,
    val toolType: String,
    val toolSubtype: String?,
    val arguments: Map<String, Any?>,
    val timestamp: String,
    val timestampUserAction: String,
    val userActionCorrelationId: String,
)

/**
 * Payload for a tool call that has completed successfully.
 */
public data class ToolCallCompletedPayload(
    val eventUid: String,
    val toolCallId: String,
    val toolName: String,
    val toolType: String,
    val toolSubtype: String?,
    val arguments: Map<String, Any?>,

    /**
     * The tool call result. For server-originated completions, this is parsed from JSON and may be a [String], [Long],
     * [Double], [Boolean], [Map]<String, Any?>, [List]<Any?>, or `null`. For client tool auto-completions, this is the
     * [String] returned from [ToolCallHandler.onStart].
     */
    val result: Any?,
    val executionTime: Long?,
    val timestamp: String,
    val timestampUserAction: String,
    val userActionCorrelationId: String,

    /** File names accessed during the tool call, if applicable (RAG tools only). */
    val documentsAccessed: List<String>? = null,
)

/**
 * Payload for a tool call that has failed.
 */
public data class ToolCallFailedPayload(
    val eventUid: String,
    val toolCallId: String,
    val toolName: String,
    val toolType: String,
    val toolSubtype: String?,
    val arguments: Map<String, Any?>,
    val errorMessage: String,
    val executionTime: Long?,
    val timestamp: String,
    val timestampUserAction: String,
    val userActionCorrelationId: String,
)
