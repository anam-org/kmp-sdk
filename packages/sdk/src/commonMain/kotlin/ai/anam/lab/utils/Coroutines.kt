package ai.anam.lab.utils

import io.ktor.utils.io.CancellationException

internal inline fun <R> cancellableRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        // If we detect a CancellationException, we should not be trying to catch/handle it.
        throw c
    } catch (e: Exception) {
        Result.failure(e)
    }
}
