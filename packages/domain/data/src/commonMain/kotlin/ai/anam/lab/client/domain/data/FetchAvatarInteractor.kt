package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason

fun interface FetchAvatarInteractor {
    suspend operator fun invoke(id: String): Either<AvatarErrorReason, Avatar>
}
