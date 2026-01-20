package ai.anam.lab.client.domain.session

import kotlinx.coroutines.flow.Flow

fun interface ObserveIsSessionActiveInteractor {
    suspend operator fun invoke(): Flow<Boolean>
}
