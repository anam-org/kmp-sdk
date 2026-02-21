package ai.anam.lab.webrtc

import ai.anam.lab.Message
import ai.anam.lab.MessageRole
import ai.anam.lab.MessageRole.Persona
import ai.anam.lab.MessageRole.User
import ai.anam.lab.ReasoningMessage
import ai.anam.lab.ToolEvent
import ai.anam.lab.api.ClientConfig
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.RTCIceServer as ApiIceServer
import ai.anam.lab.api.RTCSessionDescriptionType
import ai.anam.lab.api.SignalMessagePayload.RTCIceCandidate
import ai.anam.lab.api.SignalMessagePayload.RTCSessionDescription
import com.shepeliev.webrtckmp.BundlePolicy
import com.shepeliev.webrtckmp.ContinualGatheringPolicy
import com.shepeliev.webrtckmp.IceCandidate
import com.shepeliev.webrtckmp.IceServer
import com.shepeliev.webrtckmp.IceTransportPolicy
import com.shepeliev.webrtckmp.RtcConfiguration
import com.shepeliev.webrtckmp.RtcpMuxPolicy
import com.shepeliev.webrtckmp.SessionDescription
import com.shepeliev.webrtckmp.SessionDescriptionType
import com.shepeliev.webrtckmp.TlsCertPolicy

/**
 * Extension to convert a given [SessionDescription] instance into the equivalent [RTCSessionDescription].
 */
internal fun SessionDescription.toRTCSessionDescription() = RTCSessionDescription(
    sdp = sdp,
    type = type.toRTCSessionDescriptionType(),
)

/**
 * Extension to convert a given [RTCSessionDescription] instance into the equivalent [SessionDescription].
 */
internal fun RTCSessionDescription.toSessionDescription() = SessionDescription(
    sdp = sdp,
    type = type.toSessionDescriptionType(),
)

/**
 * Extension to convert a given [SessionDescriptionType] instance into the equivalent [RTCSessionDescriptionType].
 */
internal fun SessionDescriptionType.toRTCSessionDescriptionType() = when (this) {
    SessionDescriptionType.Offer -> RTCSessionDescriptionType.Offer
    SessionDescriptionType.Answer -> RTCSessionDescriptionType.Answer
    SessionDescriptionType.Pranswer -> RTCSessionDescriptionType.ProvisionalAnswer
    SessionDescriptionType.Rollback -> RTCSessionDescriptionType.Rollback
}

/**
 * Extension to convert a given [RTCSessionDescriptionType] instance into the equivalent [SessionDescriptionType].
 */
internal fun RTCSessionDescriptionType.toSessionDescriptionType() = when (this) {
    RTCSessionDescriptionType.Offer -> SessionDescriptionType.Offer
    RTCSessionDescriptionType.Answer -> SessionDescriptionType.Answer
    RTCSessionDescriptionType.ProvisionalAnswer -> SessionDescriptionType.Pranswer
    RTCSessionDescriptionType.Rollback -> SessionDescriptionType.Rollback
}

/**
 * Extension to convert a given [IceCandidate] instance into the equivalent [RTCIceCandidate].
 */
internal fun IceCandidate.toRTCIceCandidate() = RTCIceCandidate(
    candidate = candidate,
    sdpMid = sdpMid,
    sdpMLineIndex = sdpMLineIndex,
)

/**
 * Extension to convert a given [RTCIceCandidate] instance into the equivalent [IceCandidate].
 */
internal fun RTCIceCandidate.toIceCandidate() = IceCandidate(
    candidate = candidate,
    sdpMid = sdpMid ?: "",
    sdpMLineIndex = sdpMLineIndex ?: 0,
)

/**
 * Converts an API [ClientConfig] to a WebRTC [RtcConfiguration].
 */
internal fun ClientConfig.toRtcConfiguration(): RtcConfiguration {
    return RtcConfiguration(
        iceServers = iceServers.map { it.toIceServer() },
        bundlePolicy = BundlePolicy.Balanced,
        iceTransportPolicy = IceTransportPolicy.All,
        rtcpMuxPolicy = RtcpMuxPolicy.Require,
        continualGatheringPolicy = ContinualGatheringPolicy.GatherOnce,
    )
}

/**
 * Converts an API [ApiIceServer] to a WebRTC [IceServer].
 */
internal fun ApiIceServer.toIceServer(): IceServer {
    return IceServer(
        urls = urls,
        username = username ?: "",
        password = credential ?: "",
        tlsCertPolicy = TlsCertPolicy.TlsCertPolicySecure,
    )
}

/**
 * Converts an API [DataChannelMessagePayload.TextMessage] to a [Message].
 */
internal fun DataChannelMessagePayload.TextMessage.toMessage(): Message {
    return Message(
        id = "$role::$id",
        content = content,
        role = MessageRole.fromString(role),
        endOfSpeech = endOfSpeech,
        interrupted = interrupted,
    )
}

/**
 * Converts an API [DataChannelMessagePayload.ReasoningTextMessage] to a [ReasoningMessage].
 */
internal fun DataChannelMessagePayload.ReasoningTextMessage.toReasoningMessage(): ReasoningMessage {
    return ReasoningMessage(
        id = "$role::$id",
        content = content,
        role = MessageRole.fromString(role),
        endOfThought = endOfThought,
    )
}

/**
 * Converts a given String into the [MessageRole] equivalent.
 */
internal fun MessageRole.Companion.fromString(value: String): MessageRole {
    return when (value) {
        "user" -> User
        "persona" -> Persona
        else -> error("Invalid MessageRole value: $value")
    }
}

/**
 * Converts a [DataChannelMessagePayload.ClientToolMessage] to a [ToolEvent].
 */
internal fun DataChannelMessagePayload.ClientToolMessage.toToolEvent(): ToolEvent {
    return ToolEvent(
        eventUid = id,
        sessionId = sessionId,
        eventName = name,
        eventData = data,
        timestamp = timestamp,
        timestampUserAction = targetTimestamp,
        userActionCorrelationId = correlationId,
    )
}
