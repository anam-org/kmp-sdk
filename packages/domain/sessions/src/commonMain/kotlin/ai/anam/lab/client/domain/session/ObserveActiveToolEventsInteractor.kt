package ai.anam.lab.client.domain.session

import ai.anam.lab.ToolEvent
import kotlinx.coroutines.flow.Flow

fun interface ObserveActiveToolEventsInteractor {
    suspend operator fun invoke(): Flow<ToolEvent>
}
