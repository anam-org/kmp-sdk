package ai.anam.lab.metrics

import ai.anam.lab.ConnectionClosedReason

/**
 * Enum of metric measurement names sent to the Anam metrics API.
 */
internal enum class ClientMetric(val metricName: String) {
    Error("client_error"),
    ConnectionClosed("client_connection_closed"),
    ConnectionEstablished("client_connection_established"),
    SessionAttempt("client_session_attempt"),
    SessionSuccess("client_session_success"),
}

/**
 * Tag key constants sent alongside client metrics.
 */
internal object ClientTags {
    const val CLIENT = "client"
    const val VERSION = "version"
    const val SESSION_ID = "session_id"
    const val ORGANIZATION_ID = "organization_id"
    const val ATTEMPT_CORRELATION_ID = "attempt_correlation_id"
    const val REASON = "reason"
    const val ERROR = "error"
}

/**
 * Maps a [ConnectionClosedReason][ai.anam.lab.ConnectionClosedReason] to a bounded, human-readable string suitable
 * for use as a metric tag value.
 */
internal fun ConnectionClosedReason.toMetricValue(): String = when (this) {
    is ConnectionClosedReason.Normal -> "normal"
    is ConnectionClosedReason.SignallingClientConnectionFailure -> "signalling_failure"
    is ConnectionClosedReason.WebRtcFailure -> "webrtc_failure"
    is ConnectionClosedReason.ServerConnectionClosed -> reason
    is ConnectionClosedReason.MicrophonePermissionDenied -> "microphone_permission_denied"
}

/**
 * Contextual information attached to metrics for a given session.
 */
internal data class MetricsContext(
    val sessionId: String? = null,
    val organizationId: String? = null,
    val attemptCorrelationId: String? = null,
)
