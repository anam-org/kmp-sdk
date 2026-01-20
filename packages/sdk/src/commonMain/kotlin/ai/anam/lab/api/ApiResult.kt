package ai.anam.lab.api

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * Represents the result of an API call, which can be either a [Success] or an [Error].
 *
 * @param T The type of the successful response data.
 */
internal sealed class ApiResult<out T> {
    /**
     * Represents a successful API response.
     *
     * @param data The response data.
     */
    internal data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * Represents a failed API call.
     */
    internal sealed class Error : ApiResult<Nothing>() {
        /**
         * Represents an HTTP error response from the server (4xx, 5xx status codes).
         *
         * @param statusCode The HTTP status code.
         * @param message The error message from the server response (parsed from JSON if available).
         * @param rawResponse The raw response body, if available.
         */
        internal data class HttpError(val statusCode: Int, val message: String?, val rawResponse: String? = null) :
            Error()

        /**
         * Represents a serialization error, typically when parsing JSON responses fails.
         *
         * @param message Human-readable error message.
         * @param cause The underlying exception that caused this error.
         */
        internal data class SerializationError(val message: String, val cause: Throwable? = null) : Error()

        /**
         * Represents an unknown or unexpected error.
         *
         * @param message Human-readable error message.
         * @param cause The underlying exception that caused this error.
         */
        internal data class UnknownError(val message: String, val cause: Throwable? = null) : Error()
    }
}

/**
 * Represents the error response structure returned by the server in JSON format.
 */
@Serializable
internal data class ApiErrorResponse(
    @SerialName("error")
    val error: String,

    @SerialName("message")
    val message: String? = null,
)

/**
 * Wraps an API call in a try-catch block and converts exceptions to [ApiResult].
 *
 * This function handles:
 * - [ResponseException]: HTTP errors (4xx, 5xx) with JSON error message parsing
 * - [SerializationException]: JSON parsing failures
 * - Network exceptions (timeout, connectivity issues): Network connectivity issues
 * - Other exceptions: Unexpected errors
 *
 * @param block The suspend function that performs the API call.
 * @return [ApiResult.Success] with the result on success, or [ApiResult.Error] on failure.
 */
internal suspend inline fun <T> apiCall(block: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(block())
    } catch (e: ResponseException) {
        // HTTP error (4xx, 5xx)
        val statusCode = e.response.status.value
        val rawResponse = try {
            e.response.body<String>()
        } catch (_: Exception) {
            null
        }

        // If we received a JSON error response, parse it. Otherwise, use a generic error message.
        val errorMessage = rawResponse?.let { body ->
            try {
                defaultJsonConfiguration.decodeFromString<ApiErrorResponse>(body).message
            } catch (_: SerializationException) {
                null
            }
        }

        ApiResult.Error.HttpError(
            statusCode = statusCode,
            message = errorMessage,
            rawResponse = rawResponse,
        )
    } catch (e: SerializationException) {
        // JSON parsing error
        ApiResult.Error.SerializationError(
            message = e.message ?: "Failed to parse response",
            cause = e,
        )
    } catch (e: Exception) {
        // Never attempt to swallow a CancellationException.
        if (e is CancellationException) {
            throw e
        }

        // Unknown error
        ApiResult.Error.UnknownError(
            message = e.message ?: "An unknown error occurred",
            cause = e,
        )
    }
}

/**
 * Returns a human-readable error message from this [ApiResult.Error].
 *
 * For [HttpError], returns the parsed message if available, otherwise a generic message based on the status code.
 * For [SerializationError] and [UnknownError], returns their message directly.
 */
internal val ApiResult.Error.message: String
    get() = when (val error = this) {
        is ApiResult.Error.HttpError -> error.message ?: "HTTP error ${error.statusCode}"
        is ApiResult.Error.SerializationError -> error.message
        is ApiResult.Error.UnknownError -> error.message
    }

/**
 * Returns the underlying [Throwable] that caused this error, if available.
 *
 * Returns `null` for [HttpError] (which doesn't have a cause).
 * Returns the cause for [SerializationError] and [UnknownError].
 */
internal val ApiResult.Error.cause: Throwable?
    get() = when (val error = this) {
        is ApiResult.Error.HttpError -> null
        is ApiResult.Error.SerializationError -> error.cause
        is ApiResult.Error.UnknownError -> error.cause
    }
