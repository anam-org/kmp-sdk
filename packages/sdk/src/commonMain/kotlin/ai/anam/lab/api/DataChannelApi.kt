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
import kotlinx.serialization.json.JsonObject
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

    @SerialName("reasoningText")
    ReasoningText,

    @SerialName("toolCallStarted")
    ToolCallStarted,

    @SerialName("toolCallCompleted")
    ToolCallCompleted,

    @SerialName("toolCallFailed")
    ToolCallFailed,
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
    data class ReasoningTextMessage(
        @SerialName("message_id")
        val id: String,

        @SerialName("content_index")
        val index: Int,

        @SerialName("content")
        val content: String,

        @SerialName("role")
        val role: String,

        @SerialName("end_of_thought")
        val endOfThought: Boolean,
    ) : DataChannelMessagePayload

    /**
     * Shared contract for the new tool call message format. All three lifecycle messages (Started, Completed, Failed)
     * share these common fields from the wire protocol.
     */
    sealed interface ToolCallMessage : DataChannelMessagePayload {
        val eventUid: String
        val sessionId: String
        val toolCallId: String
        val toolName: String
        val toolType: String
        val toolSubtype: String?
        val arguments: JsonObject
        val timestamp: String
        val timestampUserAction: String
        val userActionCorrelationId: String
        val usedOutsideEngine: Boolean
    }

    @Serializable
    data class ToolCallStartedMessage(
        @SerialName("event_uid")
        override val eventUid: String,

        @SerialName("session_id")
        override val sessionId: String,

        @SerialName("tool_call_id")
        override val toolCallId: String,

        @SerialName("tool_name")
        override val toolName: String,

        @SerialName("tool_type")
        override val toolType: String,

        @SerialName("tool_subtype")
        override val toolSubtype: String? = null,

        @SerialName("arguments")
        override val arguments: JsonObject,

        @SerialName("timestamp")
        override val timestamp: String,

        @SerialName("timestamp_user_action")
        override val timestampUserAction: String,

        @SerialName("user_action_correlation_id")
        override val userActionCorrelationId: String,

        @SerialName("used_outside_engine")
        override val usedOutsideEngine: Boolean,
    ) : ToolCallMessage

    @Serializable
    data class ToolCallCompletedMessage(
        @SerialName("event_uid")
        override val eventUid: String,

        @SerialName("session_id")
        override val sessionId: String,

        @SerialName("tool_call_id")
        override val toolCallId: String,

        @SerialName("tool_name")
        override val toolName: String,

        @SerialName("tool_type")
        override val toolType: String,

        @SerialName("tool_subtype")
        override val toolSubtype: String? = null,

        @SerialName("arguments")
        override val arguments: JsonObject,

        @SerialName("timestamp")
        override val timestamp: String,

        @SerialName("timestamp_user_action")
        override val timestampUserAction: String,

        @SerialName("user_action_correlation_id")
        override val userActionCorrelationId: String,

        @SerialName("used_outside_engine")
        override val usedOutsideEngine: Boolean,

        @SerialName("result")
        val result: JsonElement,

        @SerialName("documents_accessed")
        val documentsAccessed: List<String>? = null,
    ) : ToolCallMessage

    @Serializable
    data class ToolCallFailedMessage(
        @SerialName("event_uid")
        override val eventUid: String,

        @SerialName("session_id")
        override val sessionId: String,

        @SerialName("tool_call_id")
        override val toolCallId: String,

        @SerialName("tool_name")
        override val toolName: String,

        @SerialName("tool_type")
        override val toolType: String,

        @SerialName("tool_subtype")
        override val toolSubtype: String? = null,

        @SerialName("arguments")
        override val arguments: JsonObject,

        @SerialName("timestamp")
        override val timestamp: String,

        @SerialName("timestamp_user_action")
        override val timestampUserAction: String,

        @SerialName("user_action_correlation_id")
        override val userActionCorrelationId: String,

        @SerialName("used_outside_engine")
        override val usedOutsideEngine: Boolean,

        @SerialName("error_message")
        val errorMessage: String,
    ) : ToolCallMessage
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
            jsonObject.containsKey("end_of_thought") -> DataChannelMessagePayload.ReasoningTextMessage.serializer()
            jsonObject.containsKey("message_id") -> DataChannelMessagePayload.TextMessage.serializer()
            jsonObject.containsKey("tool_call_id") -> {
                when {
                    jsonObject.containsKey("result") ->
                        DataChannelMessagePayload.ToolCallCompletedMessage.serializer()
                    jsonObject.containsKey("error_message") ->
                        DataChannelMessagePayload.ToolCallFailedMessage.serializer()
                    else -> DataChannelMessagePayload.ToolCallStartedMessage.serializer()
                }
            }
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
