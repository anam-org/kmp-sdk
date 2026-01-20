package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.data.models.LlmErrorReason
import ai.anam.lab.client.core.data.models.PagedList

fun interface FetchLlmsInteractor {
    suspend operator fun invoke(
        page: Int,
        perPage: Int,
        query: String?,
        includeDefaults: Boolean?,
    ): Either<LlmErrorReason, PagedList<Llm>>
}
