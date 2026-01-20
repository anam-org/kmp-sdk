package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.data.models.LlmErrorReason

fun interface FetchLlmInteractor {
    suspend operator fun invoke(id: String): Either<LlmErrorReason, Llm>
}
