package ai.anam.lab.client.core.data.models

import ai.anam.lab.client.core.api.ApiResult
import ai.anam.lab.client.core.api.Llm as ApiLlm
import ai.anam.lab.client.core.api.PagedList as ApiPagedList
import ai.anam.lab.client.core.common.Either
import kotlin.time.Instant

/**
 * Represents a single LLM instance.
 */
data class Llm(
    val id: String,
    val displayName: String,
    val description: String?,
    val llmFormat: String,
    val modelName: String?,
    val temperature: Double?,
    val maxTokens: Int?,
    val deploymentName: String?,
    val apiVersion: String?,
    val displayTags: List<String>,
    val isDefault: Boolean,
    val isGlobal: Boolean,
    val createdByOrganizationId: String?,
    val createdAt: Instant,
    val updatedAt: Instant?,
)

/**
 * Represents the reason why fetching an LLM failed.
 */
sealed interface LlmErrorReason {
    /**
     * Represents the case where the request was not authorized (401 error).
     */
    data object NotAuthorized : LlmErrorReason

    /**
     * Represents the case where the requested LLM was not found (404 error).
     */
    data object LlmNotFound : LlmErrorReason

    /**
     * Represents an unknown error that occurred while fetching the LLM.
     *
     * @param message A human-readable error message describing what went wrong.
     * @param cause The underlying exception that caused this error, if available.
     */
    data class Unknown(val message: String, val cause: Throwable? = null) : LlmErrorReason
}

/**
 * Extension function to convert an API model into our application model.
 */
fun ApiLlm.toLlm() = Llm(
    id = id,
    displayName = displayName,
    description = description,
    llmFormat = llmFormat,
    modelName = modelName,
    temperature = temperature,
    maxTokens = maxTokens,
    deploymentName = deploymentName,
    apiVersion = apiVersion,
    displayTags = displayTags,
    isDefault = isDefault,
    isGlobal = isGlobal,
    createdByOrganizationId = createdByOrganizationId,
    createdAt = Instant.parse(createdAt),
    updatedAt = updatedAt?.let { Instant.parse(it) },
)

/**
 * Extension function to convert an [ApiResult] containing an [ApiLlm] to an [Either] containing
 * an [Llm] or [LlmErrorReason].
 *
 * - [ApiResult.Success] is converted to [Either.Right] with the LLM converted using [toLlm]
 * - [ApiResult.Error] with HTTP 401 is converted to [Either.Left] with [LlmErrorReason.NotAuthorized]
 * - [ApiResult.Error] with HTTP 404 is converted to [Either.Left] with [LlmErrorReason.LlmNotFound]
 * - Other [ApiResult.Error] instances are converted to [Either.Left] with [LlmErrorReason.Unknown]
 */
fun ApiResult<ApiLlm>.toLlmResult(): Either<LlmErrorReason, Llm> {
    return when (this) {
        is ApiResult.Success -> Either.Right(data.toLlm())
        is ApiResult.Error -> Either.Left(toLlmErrorReason())
    }
}

/**
 * Extension function to convert an [ApiResult] containing an [ApiPagedList] of [ApiLlm] to an [Either] containing
 * a [PagedList] of [Llm] or [LlmErrorReason].
 *
 * - [ApiResult.Success] is converted to [Either.Right] with the list converted using [toPagedList] and [toLlm]
 * - [ApiResult.Error] is converted to [Either.Left] using [toLlmErrorReason]
 */
fun ApiResult<ApiPagedList<ApiLlm>>.toLlmListResult(): Either<LlmErrorReason, PagedList<Llm>> {
    return when (this) {
        is ApiResult.Success -> Either.Right(data.toPagedList { it.toLlm() })
        is ApiResult.Error -> Either.Left(toLlmErrorReason())
    }
}

/**
 * Converts an [ApiResult.Error] to an [LlmErrorReason].
 *
 * - HTTP 401 errors are converted to [LlmErrorReason.NotAuthorized]
 * - HTTP 404 errors are converted to [LlmErrorReason.LlmNotFound]
 * - Other errors are converted to [LlmErrorReason.Unknown] with the error message and cause
 */
private fun ApiResult.Error.toLlmErrorReason(): LlmErrorReason {
    return when (this) {
        is ApiResult.Error.HttpError -> {
            when (statusCode) {
                401 -> LlmErrorReason.NotAuthorized
                404 -> LlmErrorReason.LlmNotFound
                else -> LlmErrorReason.Unknown(message = message)
            }
        }
        is ApiResult.Error.SerializationError,
        is ApiResult.Error.UnknownError,
        -> {
            LlmErrorReason.Unknown(message = message, cause = cause)
        }
    }
}
