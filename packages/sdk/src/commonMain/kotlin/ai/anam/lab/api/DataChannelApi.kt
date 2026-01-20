@file:OptIn(ExperimentalSerializationApi::class)

package ai.anam.lab.api

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

@Serializable
internal data class DataChannelMessage(
    @SerialName("messageType")
    val type: DataChannelMessageType,

    @SerialName("data")
    val data: DataChannelMessagePayload,
)

@Serializable
internal enum class DataChannelMessageType {
    @SerialName("speechText")
    SpeechText,

    @SerialName("clientToolEvent")
    ClientToolEvent,
}

@Serializable(with = DataChannelMessagePayloadSerializer::class)
internal sealed interface DataChannelMessagePayload {

    @Serializable
    data class TextMessage(
        @SerialName("message_id")
        val id: String,

        @SerialName("content_index")
        val index: Int,

        @SerialName("content")
        val content: String,

        @SerialName("role")
        val role: String,

        @SerialName("end_of_speech")
        val endOfSpeech: Boolean,

        @SerialName("interrupted")
        val interrupted: Boolean,
    ) : DataChannelMessagePayload

    @Serializable
    data class ClientToolMessage(
        // Unique ID for this event
        @SerialName("event_uid")
        val id: String,

        @SerialName("session_id")
        val sessionId: String,

        // The tool name (e.g., "redirect")
        @SerialName("event_name")
        val name: String,

        // LLM-generated parameters
        @SerialName("event_data")
        val data: String,

        @SerialName("timestamp")
        val timestamp: String,

        @SerialName("timestamp_user_action")
        val targetTimestamp: String,

        @SerialName("user_action_correlation_id")
        val correlationId: String,

        @SerialName("used_outside_engine")
        val usedOutsideEngine: Boolean,
    ) : DataChannelMessagePayload
}

@Serializable(with = UserDataMessageSerializer::class)
internal sealed interface UserDataMessage {

    @Serializable
    data class UserTextMessage(
        @SerialName("content")
        val content: String,

        @SerialName("timestamp")
        val timestamp: String,

        @SerialName("session_id")
        val sessionId: String,

        @EncodeDefault
        @SerialName("message_type")
        val type: String = "speech",
    ) : UserDataMessage

    @Serializable
    data class PersonaInterruptMessage(
        @SerialName("timestamp")
        val timestamp: String,

        @SerialName("session_id")
        val sessionId: String,

        @EncodeDefault
        @SerialName("message_type")
        val type: String = "interrupt",
    ) : UserDataMessage
}

internal object DataChannelMessagePayloadSerializer :
    JsonContentPolymorphicSerializer<DataChannelMessagePayload>(
        DataChannelMessagePayload::class,
    ),
    KSerializer<DataChannelMessagePayload> {

    override fun selectDeserializer(element: JsonElement): KSerializer<out DataChannelMessagePayload> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("message_id") -> DataChannelMessagePayload.TextMessage.serializer()
            jsonObject.containsKey("event_uid") -> DataChannelMessagePayload.ClientToolMessage.serializer()
            else -> throw SerializationException("Unknown DataChannelMessage type: $jsonObject")
        }
    }
}

internal object UserDataMessageSerializer :
    JsonContentPolymorphicSerializer<UserDataMessage>(UserDataMessage::class),
    KSerializer<UserDataMessage> {

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<UserDataMessage> {
        val jsonObject = element.jsonObject
        return when {
            jsonObject.containsKey("content") -> UserDataMessage.UserTextMessage.serializer()
            else -> UserDataMessage.PersonaInterruptMessage.serializer()
        }
    }
}
