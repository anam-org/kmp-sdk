package ai.anam.lab.client.domain.session

import ai.anam.lab.Message
import kotlinx.coroutines.flow.Flow

fun interface ObserveActiveMessageHistoryInteractor {
    suspend operator fun invoke(): Flow<List<Message>>
}
