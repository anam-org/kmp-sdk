package ai.anam.lab.client.core.data.models

import ai.anam.lab.client.core.api.ApiResult
import ai.anam.lab.client.core.api.Avatar as ApiAvatar
import ai.anam.lab.client.core.api.PagedList as ApiPagedList
import ai.anam.lab.client.core.common.Either
import kotlin.time.Instant

/**
 * Represents a single Avatar instance.
 */
data class Avatar(
    val id: String,
    val displayName: String,
    val imageUrl: String,
    val videoUrl: String,
    val updatedAt: Instant,
    val createdByOrganizationId: String? = null,
)

/**
 * Returns `true` if this avatar is a one-shot avatar (created by an organization).
 */
fun Avatar.isOneShot(): Boolean = createdByOrganizationId != null

/**
 * Represents the reason why fetching an avatar failed.
 */
sealed interface AvatarErrorReason {
    /**
     * Represents the case where the request was not authorized (401 error).
     */
    data object NotAuthorized : AvatarErrorReason

    /**
     * Represents a bad request error (400 error).
     */
    data object BadRequest : AvatarErrorReason

    /**
     * Represents a forbidden error (403 error).
     */
    data object Forbidden : AvatarErrorReason

    /**
     * Represents the case where the requested avatar was not found (404 error).
     */
    data object AvatarNotFound : AvatarErrorReason

    /**
     * Represents an unknown error that occurred while fetching the avatar.
     *
     * @param message A human-readable error message describing what went wrong.
     * @param cause The underlying exception that caused this error, if available.
     */
    data class Unknown(val message: String, val cause: Throwable? = null) : AvatarErrorReason
}

/**
 * Extension function to convert an API model into our application model.
 */
fun ApiAvatar.toAvatar() = Avatar(
    id = id,
    displayName = displayName,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    updatedAt = updatedAt,
    createdByOrganizationId = createdByOrganizationId,
)

/**
 * Extension function to convert an [ApiResult] containing an [ApiAvatar] to an [Either] containing
 * an [Avatar] or [AvatarErrorReason].
 *
 * - [ApiResult.Success] is converted to [Either.Right] with the avatar converted using [toAvatar]
 * - [ApiResult.Error] with HTTP 401 is converted to [Either.Left] with [AvatarErrorReason.NotAuthorized]
 * - [ApiResult.Error] with HTTP 404 is converted to [Either.Left] with [AvatarErrorReason.AvatarNotFound]
 * - Other [ApiResult.Error] instances are converted to [Either.Left] with [AvatarErrorReason.Unknown]
 */
fun ApiResult<ApiAvatar>.toAvatarResult(): Either<AvatarErrorReason, Avatar> {
    return when (this) {
        is ApiResult.Success -> Either.Right(data.toAvatar())
        is ApiResult.Error -> Either.Left(toAvatarErrorReason())
    }
}

/**
 * Extension function to convert an [ApiResult] containing an [ApiPagedList] of [ApiAvatar] to an [Either] containing
 * a [PagedList] of [Avatar] or [AvatarErrorReason].
 *
 * - [ApiResult.Success] is converted to [Either.Right] with the list converted using [toPagedList] and [toAvatar]
 * - [ApiResult.Error] is converted to [Either.Left] using [toAvatarErrorReason]
 */
fun ApiResult<ApiPagedList<ApiAvatar>>.toAvatarListResult(): Either<AvatarErrorReason, PagedList<Avatar>> {
    return when (this) {
        is ApiResult.Success -> Either.Right(data.toPagedList { it.toAvatar() })
        is ApiResult.Error -> Either.Left(toAvatarErrorReason())
    }
}

fun ApiResult<Unit>.toAvatarUnitResult(): Either<AvatarErrorReason, Unit> {
    return when (this) {
        is ApiResult.Success -> Either.Right(Unit)
        is ApiResult.Error -> Either.Left(toAvatarErrorReason())
    }
}

/**
 * Converts an [ApiResult.Error] to an [AvatarErrorReason].
 *
 * - HTTP 401 errors are converted to [AvatarErrorReason.NotAuthorized]
 * - HTTP 404 errors are converted to [AvatarErrorReason.AvatarNotFound]
 * - Other errors are converted to [AvatarErrorReason.Unknown] with the error message and cause
 */
private fun ApiResult.Error.toAvatarErrorReason(): AvatarErrorReason {
    return when (this) {
        is ApiResult.Error.HttpError -> {
            when (statusCode) {
                400 -> AvatarErrorReason.BadRequest
                401 -> AvatarErrorReason.NotAuthorized
                403 -> AvatarErrorReason.Forbidden
                404 -> AvatarErrorReason.AvatarNotFound
                else -> AvatarErrorReason.Unknown(message = message)
            }
        }
        is ApiResult.Error.SerializationError,
        is ApiResult.Error.UnknownError,
        -> {
            AvatarErrorReason.Unknown(message = message, cause = cause)
        }
    }
}
