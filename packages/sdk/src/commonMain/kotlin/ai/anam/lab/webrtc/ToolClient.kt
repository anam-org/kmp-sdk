package ai.anam.lab.webrtc

import ai.anam.lab.ToolEvent
import ai.anam.lab.api.DataChannelMessagePayload
import ai.anam.lab.api.DataChannelMessageType
import ai.anam.lab.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

internal interface ToolClient {
    val toolEvents: Flow<ToolEvent>
}

internal class ToolClientImpl(client: StreamingClient, private val logger: Logger) : ToolClient {

    override val toolEvents: Flow<ToolEvent> = client.dataChannelMessages
        .filter { it.type == DataChannelMessageType.ClientToolEvent }
        .map { it.data as DataChannelMessagePayload.ClientToolMessage }
        .map { message ->
            message.toToolEvent().also { logger.i(TAG) { "ToolEvent: $it" } }
        }

    private companion object {
        const val TAG = "ToolClient"
    }
}
