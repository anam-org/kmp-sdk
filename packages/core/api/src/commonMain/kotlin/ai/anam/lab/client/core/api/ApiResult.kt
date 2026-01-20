package ai.anam.lab.client.core.api

import ai.anam.lab.client.core.http.defaultJsonConfiguration
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

/**
 * Represents the result of an API call, which can be either a [Success] or an [Error].
 *
 * @param T The type of the successful response data.
 */
sealed class ApiResult<out T> {
    /**
     * Represents a successful API response.
     *
     * @param data The response data.
     */
    data class Success<T>(val data: T) : ApiResult<T>()

    /**
     * Represents a failed API call.
     *
     * All error types have a [message] and optional [cause] that can be accessed directly.
     */
    sealed class Error : ApiResult<Nothing>() {
        /**
         * A human-readable error message describing what went wrong.
         */
        abstract val message: String

        /**
         * The underlying exception that caused this error, if available.
         */
        abstract val cause: Throwable?

        /**
         * Represents an HTTP error response from the server (4xx, 5xx status codes).
         *
         * @param statusCode The HTTP status code.
         * @param message The error message from the server response (parsed from JSON if available), or a generic message based on the status code.
         * @param rawResponse The raw response body, if available.
         * @param cause The underlying exception that caused this error, if available.
         */
        data class HttpError(
            val statusCode: Int,
            override val message: String,
            val rawResponse: String? = null,
            override val cause: Throwable? = null,
        ) : Error()

        /**
         * Represents a serialization error, typically when parsing JSON responses fails.
         *
         * @param message Human-readable error message.
         * @param cause The underlying exception that caused this error.
         */
        data class SerializationError(override val message: String, override val cause: Throwable? = null) : Error()

        /**
         * Represents an unknown or unexpected error.
         *
         * @param message Human-readable error message.
         * @param cause The underlying exception that caused this error.
         */
        data class UnknownError(override val message: String, override val cause: Throwable? = null) : Error()
    }
}

/**
 * Represents the error response structure returned by the server in JSON format.
 */
@Serializable
data class ApiErrorResponse(
    @SerialName("error")
    val error: String,
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
suspend inline fun <T> apiCall(block: suspend () -> T): ApiResult<T> {
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
                defaultJsonConfiguration.decodeFromString<ApiErrorResponse>(body).error
            } catch (_: SerializationException) {
                null
            }
        }

        ApiResult.Error.HttpError(
            statusCode = statusCode,
            message = errorMessage ?: "HTTP error $statusCode",
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
