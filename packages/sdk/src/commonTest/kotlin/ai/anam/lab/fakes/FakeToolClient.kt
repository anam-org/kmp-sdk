package ai.anam.lab.fakes

import ai.anam.lab.ToolEvent
import ai.anam.lab.webrtc.ToolClient
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

internal class FakeToolClient : ToolClient {
    private val _toolEvents = Channel<ToolEvent>(Channel.BUFFERED)
    override val toolEvents: Flow<ToolEvent> = _toolEvents.receiveAsFlow()

    suspend fun emitToolEvent(event: ToolEvent) {
        _toolEvents.send(event)
    }
}
