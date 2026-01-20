package ai.anam.lab.client.domain.data

import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentLlmIdInteractor {
    suspend operator fun invoke(): Flow<String?>
}
