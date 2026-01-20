package ai.anam.lab.client.core.data.models

import ai.anam.lab.client.core.api.ApiResult
import ai.anam.lab.client.core.api.PagedList as ApiPagedList
import ai.anam.lab.client.core.api.Voice as ApiVoice
import ai.anam.lab.client.core.common.Either
import kotlin.time.Instant

/**
 * Represents a single Voice instance.
 */
data class Voice(
    val id: String,
    val displayName: String,
    val provider: String,
    val providerVoiceId: String,
    val providerModelId: String,
    val sampleUrl: String? = null,
    val gender: String? = null,
    val country: String? = null,
    val description: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
    val createdByOrganizationId: String? = null,
)

/**
 * Represents the reason why fetching a voice failed.
 */
sealed interface VoiceErrorReason {
    /**
     * Represents the case where the request was not authorized (401 error).
     */
    data object NotAuthorized : VoiceErrorReason

    /**
     * Represents the case where the requested voice was not found (404 error).
     */
    data object VoiceNotFound : VoiceErrorReason

    /**
     * Represents an unknown error that occurred while fetching the voice.
     *
     * @param message A human-readable error message describing what went wrong.
     * @param cause The underlying exception that caused this error, if available.
     */
    data class Unknown(val message: String, val cause: Throwable? = null) : VoiceErrorReason
}

/**
 * Extension function to convert an API model into our application model.
 */
fun ApiVoice.toVoice() = Voice(
    id = id,
    displayName = displayName,
    provider = provider,
    providerVoiceId = providerVoiceId,
    providerModelId = providerModelId,
    sampleUrl = sampleUrl,
    gender = gender,
    country = country,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    createdByOrganizationId = createdByOrganizationId,
)

/**
 * Extension function to convert an [ApiResult] containing an [ApiVoice] to an [Either] containing
 * a [Voice] or [VoiceErrorReason].
 *
 * - [ApiResult.Success] is converted to [Either.Right] with the voice converted using [toVoice]
 * - [ApiResult.Error] with HTTP 401 is converted to [Either.Left] with [VoiceErrorReason.NotAuthorized]
 * - [ApiResult.Error] with HTTP 404 is converted to [Either.Left] with [VoiceErrorReason.VoiceNotFound]
 * - Other [ApiResult.Error] instances are converted to [Either.Left] with [VoiceErrorReason.Unknown]
 */
fun ApiResult<ApiVoice>.toVoiceResult(): Either<VoiceErrorReason, Voice> {
    return when (this) {
        is ApiResult.Success -> Either.Right(data.toVoice())
        is ApiResult.Error -> Either.Left(toVoiceErrorReason())
    }
}

/**
 * Extension function to convert an [ApiResult] containing an [ApiPagedList] of [ApiVoice] to an [Either] containing
 * a [PagedList] of [Voice] or [VoiceErrorReason].
 *
 * - [ApiResult.Success] is converted to [Either.Right] with the list converted using [toPagedList] and [toVoice]
 * - [ApiResult.Error] is converted to [Either.Left] using [toVoiceErrorReason]
 */
fun ApiResult<ApiPagedList<ApiVoice>>.toVoiceListResult(): Either<VoiceErrorReason, PagedList<Voice>> {
    return when (this) {
        is ApiResult.Success -> Either.Right(data.toPagedList { it.toVoice() })
        is ApiResult.Error -> Either.Left(toVoiceErrorReason())
    }
}

/**
 * Converts an [ApiResult.Error] to an [VoiceErrorReason].
 *
 * - HTTP 401 errors are converted to [VoiceErrorReason.NotAuthorized]
 * - HTTP 404 errors are converted to [VoiceErrorReason.VoiceNotFound]
 * - Other errors are converted to [VoiceErrorReason.Unknown] with the error message and cause
 */
private fun ApiResult.Error.toVoiceErrorReason(): VoiceErrorReason {
    return when (this) {
        is ApiResult.Error.HttpError -> {
            when (statusCode) {
                401 -> VoiceErrorReason.NotAuthorized
                404 -> VoiceErrorReason.VoiceNotFound
                else -> VoiceErrorReason.Unknown(message = message)
            }
        }
        is ApiResult.Error.SerializationError,
        is ApiResult.Error.UnknownError,
        -> {
            VoiceErrorReason.Unknown(message = message, cause = cause)
        }
    }
}
