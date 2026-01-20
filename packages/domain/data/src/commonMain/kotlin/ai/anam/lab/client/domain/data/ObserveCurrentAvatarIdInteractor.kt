package ai.anam.lab.client.domain.data

import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentAvatarIdInteractor {
    suspend operator fun invoke(): Flow<String?>
}
