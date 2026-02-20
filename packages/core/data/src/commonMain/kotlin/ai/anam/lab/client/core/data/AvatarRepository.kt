package ai.anam.lab.client.core.data

import ai.anam.lab.client.core.api.ApiResult
import ai.anam.lab.client.core.api.AvatarApi
import ai.anam.lab.client.core.api.apiCall
import ai.anam.lab.client.core.api.createAvatar
import ai.anam.lab.client.core.common.Either
import ai.anam.lab.client.core.coroutines.cancellableRunCatching
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason
import ai.anam.lab.client.core.data.models.PagedList
import ai.anam.lab.client.core.data.models.toAvatarListResult
import ai.anam.lab.client.core.data.models.toAvatarResult
import ai.anam.lab.client.core.data.models.toAvatarUnitResult
import ai.anam.lab.client.core.di.Dispatcher
import ai.anam.lab.client.core.di.DispatcherType
import ai.anam.lab.client.core.logging.Logger
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Inject
class AvatarRepository(
    avatarApi: Lazy<AvatarApi>,
    private val httpClient: HttpClient,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher,
    private val logger: Logger,
) {
    private val avatarApi by avatarApi

    suspend fun getAvatar(id: String): Either<AvatarErrorReason, Avatar> = withContext(ioDispatcher) {
        logger.i(TAG) { "Fetching Avatar: $id" }
        cancellableRunCatching { apiCall { avatarApi.getAvatar(id) } }
            .fold(
                onSuccess = { apiResult ->
                    logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                    apiResult.toAvatarResult()
                },
                onFailure = { exception ->
                    logger.i(TAG, exception) { "Unable to fetch avatar" }
                    Either.Left(
                        AvatarErrorReason.Unknown(
                            message = "Failed to fetch avatar: An unexpected error occurred",
                            cause = exception,
                        ),
                    )
                },
            )
    }

    suspend fun getAvatars(
        page: Int = 1,
        perPage: Int = 10,
        query: String? = null,
        onlyOneShot: Boolean? = null,
    ): Either<AvatarErrorReason, PagedList<Avatar>> = withContext(ioDispatcher) {
        logger.i(TAG) { "Fetching Avatars: page=$page, perPage=$perPage, query=$query, onlyOneShot=$onlyOneShot" }
        cancellableRunCatching {
            apiCall {
                avatarApi.getAvatars(
                    page = page,
                    perPage = perPage,
                    query = query,
                    onlyOneShot = onlyOneShot,
                )
            }
        }.fold(
            onSuccess = { apiResult ->
                logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                apiResult.toAvatarListResult()
            },
            onFailure = { exception ->
                logger.i(TAG, exception) { "Unable to fetch avatars" }
                Either.Left(
                    AvatarErrorReason.Unknown(
                        message = "Failed to fetch avatar: An unexpected error occurred",
                        cause = exception,
                    ),
                )
            },
        )
    }

    suspend fun deleteAvatar(id: String): Either<AvatarErrorReason, Unit> = withContext(ioDispatcher) {
        logger.i(TAG) { "Deleting Avatar: $id" }
        cancellableRunCatching { apiCall { avatarApi.deleteAvatar(id) } }
            .fold(
                onSuccess = { apiResult ->
                    logger.i(TAG) {
                        if (apiResult is ApiResult.Success) "Success: deleted $id" else "Error: $apiResult"
                    }
                    apiResult.toAvatarUnitResult()
                },
                onFailure = { exception ->
                    logger.i(TAG, exception) { "Unable to delete avatar" }
                    Either.Left(
                        AvatarErrorReason.Unknown(
                            message = "Failed to delete avatar: An unexpected error occurred",
                            cause = exception,
                        ),
                    )
                },
            )
    }

    suspend fun createAvatar(displayName: String, imageData: ByteArray): Either<AvatarErrorReason, Avatar> =
        withContext(ioDispatcher) {
            logger.i(TAG) { "Creating avatar: $displayName (${imageData.size} bytes)" }
            cancellableRunCatching {
                apiCall { avatarApi.createAvatar(httpClient, displayName, imageData) }
            }.fold(
                onSuccess = { apiResult ->
                    logger.i(TAG) { if (apiResult is ApiResult.Success) "Success: $apiResult" else "Error: $apiResult" }
                    apiResult.toAvatarResult()
                },
                onFailure = { exception ->
                    logger.i(TAG, exception) { "Unable to create avatar" }
                    Either.Left(
                        AvatarErrorReason.Unknown(
                            message = "Failed to create avatar: An unexpected error occurred",
                            cause = exception,
                        ),
                    )
                },
            )
        }

    private companion object {
        const val TAG = "AvatarRepository"
    }
}
