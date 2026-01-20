package ai.anam.lab.client.core.data

import ai.anam.lab.client.core.api.ApiResult
import ai.anam.lab.client.core.api.VoicesApi
import ai.anam.lab.client.core.api.apiCall
import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.coroutines.cancellableRunCatching
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.data.models.VoiceErrorReason
import ai.anam.lab.client.core.data.models.toVoiceListResult
import ai.anam.lab.client.core.data.models.toVoiceResult
import ai.anam.lab.client.core.di.Dispatcher
import ai.anam.lab.client.core.di.DispatcherType
import ai.anam.lab.client.core.logging.Logger
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Inject
class VoiceRepository(
    voiceApi: Lazy<VoicesApi>,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) {
    private val voiceApi by voiceApi

    suspend fun getVoice(id: String): Either<VoiceErrorReason, Voice> = withContext(ioDispatcher) {
        logger.i(TAG) { "Fetching Voice: $id" }
        cancellableRunCatching { apiCall { voiceApi.getVoice(id) } }
            .fold(
                onSuccess = { apiResult ->
                    logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                    apiResult.toVoiceResult()
                },
                onFailure = { exception ->
                    logger.i(TAG, exception) { "Unable to fetch voice" }
                    Either.Left(
                        VoiceErrorReason.Unknown(
                            message = "Failed to fetch voice: An unexpected error occurred",
                            cause = exception,
                        ),
                    )
                },
            )
    }

    suspend fun getVoices(
        page: Int = 1,
        perPage: Int = 10,
        query: String? = null,
    ): Either<VoiceErrorReason, PagedList<Voice>> = withContext(ioDispatcher) {
        logger.i(TAG) { "Fetching Voices: page=$page, perPage=$perPage, query=$query" }
        cancellableRunCatching {
            apiCall {
                voiceApi.getVoices(
                    page = page,
                    perPage = perPage,
                    query = query,
                )
            }
        }.fold(
            onSuccess = { apiResult ->
                logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                apiResult.toVoiceListResult()
            },
            onFailure = { exception ->
                logger.i(TAG, exception) { "Unable to fetch voices" }
                Either.Left(
                    VoiceErrorReason.Unknown(
                        message = "Failed to fetch voices: An unexpected error occurred",
                        cause = exception,
                    ),
                )
            },
        )
    }

    private companion object {
        const val TAG = "VoiceRepository"
    }
}
