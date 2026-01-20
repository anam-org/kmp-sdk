package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason
import ai.anam.lab.client.core.data.models.PagedList

fun interface FetchAvatarsInteractor {
    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        query: String?,
        onlyOneShot: Boolean?,
    ): Either<AvatarErrorReason, PagedList<Avatar>>
}
