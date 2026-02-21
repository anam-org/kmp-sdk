package ai.anam.lab.webrtc

import ai.anam.lab.ReasoningMessage
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.mapWithCurrent
import ai.anam.lab.utils.updateOrAppend
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal interface ReasoningClient {
    /**
     * A flow that represents the reasoning history of the session. This includes chain-of-thought reasoning from the
     * Persona.
     */
    val reasoningMessages: Flow<List<ReasoningMessage>>
}

internal class ReasoningClientImpl(client: StreamingClient, private val logger: Logger) : ReasoningClient {

    override val reasoningMessages: Flow<List<ReasoningMessage>> = client.dataChannelMessages
        .filter { it.type == DataChannelMessageType.ReasoningText }
        .map { it.data as DataChannelMessagePayload.ReasoningTextMessage }
        .map { listOf(it.toReasoningMessage()) }
        .mapWithCurrent { previous, next ->
            previous?.updateOrAppend(
                item = next.single(),
                id = { it.id },
                merge = { existing, incoming ->
                    existing.copy(
                        content = existing.content + incoming.content,
                        endOfThought = incoming.endOfThought,
                        version = existing.version + 1,
                    ).also { if (it.endOfThought) logger.i(TAG) { "ReasoningMessage: $it" } }
                },
            ) ?: next
        }

    private companion object {
        const val TAG = "ReasoningClient"
    }
}
