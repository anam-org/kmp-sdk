package ai.anam.lab.webrtc

import ai.anam.lab.Message
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.mapWithCurrent
import ai.anam.lab.utils.updateOrAppend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal interface MessagingClient {
    /**
     * A flow that represents the messaging history of the session. This includes transcriptions from both the User and
     * Persona.
     */
    val messages: Flow<List<Message>>
}

internal class MessagingClientImpl(client: StreamingClient, private val logger: Logger) : MessagingClient {

    override val messages: Flow<List<Message>> = client.dataChannelMessages
        .filter { it.type == DataChannelMessageType.SpeechText }
        .map { it.data as DataChannelMessagePayload.TextMessage }
        .map { listOf(it.toMessage()) }
        .mapWithCurrent { previous, next ->
            previous?.updateOrAppend(
                item = next.single(),
                id = { it.id },
                merge = { existing, incoming ->
                    existing.copy(
                        content = existing.content + incoming.content,
                        endOfSpeech = incoming.endOfSpeech,
                        interrupted = incoming.interrupted,
                        version = existing.version + 1,
                    ).also { if (it.endOfSpeech) logger.i(TAG) { "Message: $it" } }
                },
            ) ?: next
        }

    private companion object {
        const val TAG = "MessagingClient"
    }
}
