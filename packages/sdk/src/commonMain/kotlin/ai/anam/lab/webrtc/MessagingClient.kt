package ai.anam.lab.webrtc

import ai.anam.lab.Message
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.utils.Logger
import ai.anam.lab.utils.mapWithCurrent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

internal interface MessagingClient {
    /**
     * A flow that represents the messaging history of the session. This includes transcriptions from both the User and
     * Persona.
     */
    val messages: Flow<List<Message>>
}

internal class MessagingClientImpl(client: StreamingClient, private val logger: Logger) : MessagingClient {

    override val messages: Flow<List<Message>> = client.dataChannelMessages
        .map { message -> message.data }
        .mapNotNull { message ->
            when (message) {
                is DataChannelMessagePayload.TextMessage -> listOf(message.toMessage())
                else -> null
            }
        }.mapWithCurrent { previous, next ->
            return@mapWithCurrent previous?.update(next.single()) ?: next
        }

    /**
     * Extension function to update (if possible) an existing [Message]. This allows the [Message] to expand in Content
     * until we detect that it has ended.
     */
    private fun List<Message>.update(item: Message): List<Message> {
        val index = indexOfFirst { it.id == item.id }
        return if (index == -1) {
            // This list doesn't currently contain our new item. In this case, we'll simply add it to the end.
            this + item
        } else {
            // We have a new, updated version of the Message, likely containing more content. We'll update the existing
            // instance in place, and return the new updated list.
            val existing = get(index)
            val updatedItem = existing.copy(
                content = existing.content + item.content,
                endOfSpeech = item.endOfSpeech,
                interrupted = item.interrupted,
                version = existing.version + 1,
            )

            if (updatedItem.endOfSpeech) {
                logger.i(TAG) { "Message: $updatedItem" }
            }

            // Use a MutableList to avoid creating multiple intermediate lists.
            toMutableList().apply {
                set(index, updatedItem)
            }
        }
    }

    private companion object {
        const val TAG = "MessagingClient"
    }
}
