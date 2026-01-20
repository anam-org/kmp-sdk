package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason
import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentAvatarInteractor {
    suspend operator fun invoke(): Flow<Either<AvatarErrorReason, Avatar>>
}
