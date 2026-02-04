package ai.anam.lab.api

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@Serializable(with = SignalMessageSerializer::class)
internal data class SignalMessage(
    @SerialName("actionType")
    val actionType: SignalMessageType,

    @SerialName("sessionId")
    val sessionId: String,

    @SerialName("payload")
    val payload: SignalMessagePayload,
)

@Serializable
internal enum class SignalMessageType {
    @SerialName("offer")
    Offer,

    @SerialName("answer")
    Answer,

    @SerialName("icecandidate")
    IceCandidate,

    @SerialName("endsession")
    EndSession,

    @SerialName("heartbeat")
    Heartbeat,

    @SerialName("warning")
    Warning,

    @SerialName("talkinputstreaminterrupted")
    TalkStreamInterrupted,

    @SerialName("talkstream")
    TalkStreamInput,

    @SerialName("sessionready")
    SessionReady,
}

@Serializable
internal sealed interface SignalMessagePayload {

    @Serializable
    data class Offer(
        @SerialName("connectionDescription")
        val connectionDescription: RTCSessionDescription,

        @SerialName("userUid")
        val sessionId: String,
    ) : SignalMessagePayload

    @Serializable
    data class RTCSessionDescription(
        @SerialName("sdp")
        val sdp: String,

        @SerialName("type")
        val type: RTCSessionDescriptionType,
    ) : SignalMessagePayload

    @Serializable
    data class RTCIceCandidate(
        @SerialName("address")
        val address: String? = null,

        @SerialName("candidate")
        val candidate: String,

        @SerialName("component")
        val component: String? = null,

        @SerialName("foundation")
        val foundation: String? = null,

        @SerialName("port")
        val port: Int? = null,

        @SerialName("protocol")
        val protocol: String? = null,

        @SerialName("relatedAddress")
        val relatedAddress: String? = null,

        @SerialName("relatedPort")
        val relatedPort: Int? = null,

        @SerialName("sdpMid")
        val sdpMid: String? = null,

        @SerialName("sdpMLineIndex")
        val sdpMLineIndex: Int? = null,

        @SerialName("tcpType")
        val tcpType: String? = null,

        @SerialName("type")
        val type: RTCIceCandidateType? = null,

        @SerialName("usernameFragment")
        val usernameFragment: String? = null,
    ) : SignalMessagePayload

    @Serializable
    data class TalkMessage(
        @SerialName("content")
        val content: String,

        @SerialName("startOfSpeech")
        val startOfSpeech: Boolean,

        @SerialName("endOfSpeech")
        val endOfSpeech: Boolean,

        @SerialName("correlationId")
        val correlationId: String,
    ) : SignalMessagePayload

    @Serializable
    data class TalkInterrupted(
        @SerialName("correlationId")
        val correlationId: String,
    ) : SignalMessagePayload

    @Serializable
    data class Raw(val value: String = "") : SignalMessagePayload

    @Serializable
    data object Empty : SignalMessagePayload
}

internal fun SignalMessagePayload.asRaw(): String = (this as? SignalMessagePayload.Raw)?.value ?: ""

@Serializable
internal enum class RTCSessionDescriptionType {
    @SerialName("answer")
    Answer,

    @SerialName("offer")
    Offer,

    @SerialName("pranswer")
    ProvisionalAnswer,

    @SerialName("rollback")
    Rollback,
}

@Serializable
internal enum class RTCIceCandidateType {
    @SerialName("host")
    Host,

    @SerialName("srflx")
    ServerReflexive,

    @SerialName("prflx")
    PeerReflexive,

    @SerialName("relay")
    Relay,
}

private object SignalMessageSerializer : KSerializer<SignalMessage> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("SignalMessage") {
        element("actionType", serialDescriptor<SignalMessageType>())
        element("sessionId", serialDescriptor<String>())
        element("payload", serialDescriptor<SignalMessagePayload>())
    }

    override fun serialize(encoder: Encoder, value: SignalMessage) {
        encoder.encodeStructure(descriptor) {
            encodeSerializableElement(descriptor, 0, SignalMessageType.serializer(), value.actionType)
            encodeStringElement(descriptor, 1, value.sessionId)

            val rawPayload = value.payload as? SignalMessagePayload.Raw
            if (rawPayload != null) {
                encodeStringElement(descriptor, 2, rawPayload.value)
            } else {
                @Suppress("UNCHECKED_CAST")
                val serializer = getSerializerForType(value.actionType) as KSerializer<SignalMessagePayload>
                encodeSerializableElement(descriptor, 2, serializer, value.payload)
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): SignalMessage {
        return decoder.decodeStructure(descriptor) {
            lateinit var actionType: SignalMessageType
            lateinit var sessionId: String
            lateinit var payload: SignalMessagePayload

            if (decodeSequentially()) {
                // Decode in order
                actionType = decodeSerializableElement(descriptor, 0, SignalMessageType.serializer())
                sessionId = decodeStringElement(descriptor, 1)
                payload = decodeForActionType(descriptor, 2, actionType)
            } else {
                // Decode elements in any order
                while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        0 -> actionType = decodeSerializableElement(descriptor, 0, SignalMessageType.serializer())
                        1 -> sessionId = decodeStringElement(descriptor, 1)
                        2 -> payload = decodeForActionType(descriptor, 2, actionType)
                        CompositeDecoder.DECODE_DONE -> break
                        else -> error("Unexpected index: $index")
                    }
                }
            }

            SignalMessage(actionType, sessionId, payload)
        }
    }

    private fun CompositeDecoder.decodeForActionType(
        descriptor: SerialDescriptor,
        index: Int,
        actionType: SignalMessageType,
    ): SignalMessagePayload {
        return if (isRawPayload(actionType)) {
            SignalMessagePayload.Raw(decodeStringElement(descriptor, index))
        } else {
            decodeSerializableElement(descriptor, index, getSerializerForType(actionType))
        }
    }
}

private fun isRawPayload(actionType: SignalMessageType) = when (actionType) {
    SignalMessageType.EndSession -> true
    SignalMessageType.Warning -> true
    else -> false
}

private fun getSerializerForType(actionType: SignalMessageType): KSerializer<out SignalMessagePayload> {
    return when (actionType) {
        SignalMessageType.Offer -> SignalMessagePayload.Offer.serializer()
        SignalMessageType.Answer -> SignalMessagePayload.RTCSessionDescription.serializer()
        SignalMessageType.IceCandidate -> SignalMessagePayload.RTCIceCandidate.serializer()
        SignalMessageType.TalkStreamInput -> SignalMessagePayload.TalkMessage.serializer()
        SignalMessageType.TalkStreamInterrupted -> SignalMessagePayload.TalkInterrupted.serializer()
        SignalMessageType.Heartbeat, SignalMessageType.SessionReady -> SignalMessagePayload.Empty.serializer()

        // Fallback to just trying to serialize/deserialize it as a raw string.
        else -> SignalMessagePayload.Raw.serializer()
    }
}
