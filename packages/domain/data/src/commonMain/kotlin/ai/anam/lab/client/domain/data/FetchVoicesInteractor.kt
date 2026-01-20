package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.data.models.VoiceErrorReason

fun interface FetchVoicesInteractor {
    suspend operator fun invoke(page: Int, perPage: Int, query: String?): Either<VoiceErrorReason, PagedList<Voice>>
}
