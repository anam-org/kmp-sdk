package ai.anam.lab.client.domain.session

import ai.anam.lab.Session
import kotlinx.coroutines.flow.Flow

fun interface ObserveActiveSessionInteractor {
    suspend operator fun invoke(): Flow<Session?>
}
