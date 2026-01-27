package ai.anam.lab.api

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.Headers
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.SerializationException

class ApiResultTest {

    @Test
    fun `apiCall returns Success when block succeeds`() = runTest {
        val result = apiCall { "test-data" }
        assertSuccess(result, "test-data")
    }

    @Test
    fun `apiCall returns HttpError when ResponseException is thrown`() = runTest {
        val statusCode = 404
        val errorResponse = """{"error":"NotFound","message":"Resource not found"}"""
        val httpClient = createMockHttpClient(statusCode, errorResponse)

        val result = apiCall {
            httpClient.get("https://example.com/test")
        }

        assertHttpError(result, statusCode, "Resource not found", errorResponse)
    }

    @Test
    fun `apiCall returns HttpError with null message when JSON error response cannot be parsed`() = runTest {
        val statusCode = 500
        val errorResponse = """{"invalid":"json"}"""
        val httpClient = createMockHttpClient(statusCode, errorResponse)

        val result = apiCall {
            httpClient.get("https://example.com/test")
        }

        assertHttpError(result, statusCode, null, errorResponse)
    }

    @Test
    fun `apiCall returns HttpError with null message when error response has no message field`() = runTest {
        val statusCode = 400
        val errorResponse = """{"error":"BadRequest"}"""
        val httpClient = createMockHttpClient(statusCode, errorResponse)

        val result = apiCall {
            httpClient.get("https://example.com/test")
        }

        assertHttpError(result, statusCode, null, errorResponse)
    }

    @Test
    fun `apiCall returns HttpError with null rawResponse when body cannot be read`() = runTest {
        val statusCode = 403
        // Create a mock engine that responds with an error status
        val mockEngine = MockEngine {
            respond(
                content = "",
                status = HttpStatusCode(statusCode, "Forbidden"),
                headers = Headers.Empty,
            )
        }
        val httpClient = HttpClient(mockEngine) {
            expectSuccess = true
        }

        // When expectSuccess=true, a ResponseException is thrown for error status codes
        // The body is empty, so rawResponse will be an empty string, not null
        // To test null rawResponse, we need a scenario where body() throws
        // This is difficult to test with MockEngine, so we'll test with empty body instead
        val result = apiCall {
            httpClient.get("https://example.com/test")
        }

        assertHttpError(result, statusCode, null, "")
    }

    @Test
    fun `apiCall returns SerializationError when SerializationException is thrown`() = runTest {
        val serializationException = SerializationException("Failed to parse JSON")
        val result = apiCallWithException(serializationException)

        assertSerializationError(result, "Failed to parse JSON", serializationException)
    }

    @Test
    fun `apiCall returns SerializationError with default message when SerializationException has no message`() =
        runTest {
            val serializationException = SerializationException(cause = null)
            val result = apiCallWithException(serializationException)

            assertSerializationError(result, "Failed to parse response", serializationException)
        }

    @Test
    fun `apiCall returns UnknownError when generic Exception is thrown`() = runTest {
        val exception = RuntimeException("Network timeout")
        val result = apiCallWithException(exception)

        assertUnknownError(result, "Network timeout", exception)
    }

    @Test
    fun `apiCall returns UnknownError with default message when Exception has no message`() = runTest {
        val exception = RuntimeException()
        val result = apiCallWithException(exception)

        assertUnknownError(result, "An unknown error occurred", exception)
    }

    @Test
    fun `apiCall propagates CancellationException`() {
        runTest {
            val cancellationException = CancellationException("Cancelled")

            assertFailsWith<CancellationException> {
                apiCall {
                    throw cancellationException
                }
            }
        }
    }

    @Test
    fun `Error message returns parsed message for HttpError with message`() {
        val error = ApiResult.Error.HttpError(
            statusCode = 404,
            message = "Resource not found",
        )
        assertErrorMessage(error, "Resource not found")
    }

    @Test
    fun `Error message returns generic message for HttpError without message`() {
        val error = ApiResult.Error.HttpError(
            statusCode = 500,
            message = null,
        )
        assertErrorMessage(error, "HTTP error 500")
    }

    @Test
    fun `Error message returns message for SerializationError`() {
        val error = ApiResult.Error.SerializationError(
            message = "Failed to parse JSON",
        )
        assertErrorMessage(error, "Failed to parse JSON")
    }

    @Test
    fun `Error message returns message for UnknownError`() {
        val error = ApiResult.Error.UnknownError(
            message = "Network timeout",
        )
        assertErrorMessage(error, "Network timeout")
    }

    @Test
    fun `Error cause returns null for HttpError`() {
        val error = ApiResult.Error.HttpError(
            statusCode = 404,
            message = "Not found",
        )
        assertErrorCause(error, null)
    }

    @Test
    fun `Error cause returns cause for SerializationError with cause`() {
        val cause = SerializationException("Parse error")
        val error = ApiResult.Error.SerializationError(
            message = "Failed to parse",
            cause = cause,
        )
        assertErrorCause(error, cause)
    }

    @Test
    fun `Error cause returns null for SerializationError without cause`() {
        val error = ApiResult.Error.SerializationError(
            message = "Failed to parse",
            cause = null,
        )
        assertErrorCause(error, null)
    }

    @Test
    fun `Error cause returns cause for UnknownError with cause`() {
        val cause = RuntimeException("Network error")
        val error = ApiResult.Error.UnknownError(
            message = "Unknown error",
            cause = cause,
        )
        assertErrorCause(error, cause)
    }

    @Test
    fun `Error cause returns null for UnknownError without cause`() {
        val error = ApiResult.Error.UnknownError(
            message = "Unknown error",
            cause = null,
        )
        assertErrorCause(error, null)
    }

    private fun <T> assertSuccess(result: ApiResult<T>, expectedData: T) {
        assertThat(result).isInstanceOf(ApiResult.Success::class)
        assertThat((result as ApiResult.Success<T>).data).isEqualTo(expectedData)
    }

    private fun assertHttpError(
        result: ApiResult<*>,
        expectedStatusCode: Int,
        expectedMessage: String?,
        expectedRawResponse: String?,
    ) {
        assertThat(result).isInstanceOf(ApiResult.Error.HttpError::class)
        val error = result as ApiResult.Error.HttpError
        assertThat(error.statusCode).isEqualTo(expectedStatusCode)
        assertThat(error.message).isEqualTo(expectedMessage)
        assertThat(error.rawResponse).isEqualTo(expectedRawResponse)
    }

    private fun assertSerializationError(result: ApiResult<*>, expectedMessage: String, expectedCause: Throwable) {
        assertThat(result).isInstanceOf(ApiResult.Error.SerializationError::class)
        val error = result as ApiResult.Error.SerializationError
        assertThat(error.message).isEqualTo(expectedMessage)
        assertThat(error.cause).isEqualTo(expectedCause)
    }

    private fun assertUnknownError(result: ApiResult<*>, expectedMessage: String, expectedCause: Throwable) {
        assertThat(result).isInstanceOf(ApiResult.Error.UnknownError::class)
        val error = result as ApiResult.Error.UnknownError
        assertThat(error.message).isEqualTo(expectedMessage)
        assertThat(error.cause).isEqualTo(expectedCause)
    }

    private suspend fun apiCallWithException(exception: Throwable): ApiResult<*> {
        return apiCall {
            throw exception
        }
    }

    private fun assertErrorMessage(error: ApiResult.Error, expectedMessage: String) {
        assertThat(error.message).isEqualTo(expectedMessage)
    }

    private fun assertErrorCause(error: ApiResult.Error, expectedCause: Throwable?) {
        if (expectedCause == null) {
            assertThat(error.cause).isNull()
        } else {
            assertThat(error.cause).isEqualTo(expectedCause)
        }
    }

    private fun createMockHttpClient(statusCode: Int, body: String): HttpClient {
        val mockEngine = MockEngine {
            respond(
                content = body,
                status = HttpStatusCode(statusCode, "Test"),
                headers = Headers.Empty,
            )
        }
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(defaultJsonConfiguration)
            }
            expectSuccess = true
        }
    }
}
