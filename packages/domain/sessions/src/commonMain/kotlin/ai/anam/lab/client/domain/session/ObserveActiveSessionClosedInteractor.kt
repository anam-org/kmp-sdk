package ai.anam.lab.client.domain.session

import ai.anam.lab.ConnectionClosedReason
import kotlinx.coroutines.flow.Flow

fun interface ObserveActiveSessionClosedInteractor {
    suspend operator fun invoke(): Flow<ConnectionClosedReason>
}
