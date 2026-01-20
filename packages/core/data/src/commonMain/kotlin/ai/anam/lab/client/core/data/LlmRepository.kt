package ai.anam.lab.client.core.data

import ai.anam.lab.client.core.api.ApiResult
import ai.anam.lab.client.core.api.LlmApi
import ai.anam.lab.client.core.api.apiCall
import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.coroutines.cancellableRunCatching
import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.data.models.LlmErrorReason
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.data.models.toLlmListResult
import ai.anam.lab.client.core.data.models.toLlmResult
import ai.anam.lab.client.core.di.Dispatcher
import ai.anam.lab.client.core.di.DispatcherType
import ai.anam.lab.client.core.logging.Logger
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Inject
class LlmRepository(
    llmApi: Lazy<LlmApi>,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) {
    private val llmApi by llmApi

    suspend fun getLlm(id: String): Either<LlmErrorReason, Llm> = withContext(ioDispatcher) {
        logger.i(TAG) { "Fetching LLM: $id" }
        cancellableRunCatching { apiCall { llmApi.getLlm(id) } }
            .fold(
                onSuccess = { apiResult ->
                    logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                    apiResult.toLlmResult()
                },
                onFailure = { exception ->
                    logger.i(TAG, exception) { "Unable to fetch LLM" }
                    Either.Left(
                        LlmErrorReason.Unknown(
                            message = "Failed to fetch LLM: An unexpected error occurred",
                            cause = exception,
                        ),
                    )
                },
            )
    }

    suspend fun getLlms(
        page: Int = 1,
        perPage: Int = 10,
        query: String? = null,
        includeDefaults: Boolean? = null,
    ): Either<LlmErrorReason, PagedList<Llm>> = withContext(ioDispatcher) {
        logger.i(TAG) { "Fetching LLMs: page=$page, perPage=$perPage, query=$query, includeDefaults=$includeDefaults" }
        cancellableRunCatching {
            apiCall {
                llmApi.getLlms(
                    page = page,
                    perPage = perPage,
                    query = query,
                    includeDefaults = includeDefaults,
                )
            }
        }.fold(
            onSuccess = { apiResult ->
                logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                apiResult.toLlmListResult()
            },
            onFailure = { exception ->
                logger.i(TAG, exception) { "Unable to fetch LLMs" }
                Either.Left(
                    LlmErrorReason.Unknown(
                        message = "Failed to fetch LLMs: An unexpected error occurred",
                        cause = exception,
                    ),
                )
            },
        )
    }

    private companion object {
        const val TAG = "LlmRepository"
    }
}
