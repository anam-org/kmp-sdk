package ai.anam.lab.client.core.coroutines

import kotlin.coroutines.cancellation.CancellationException

inline fun <R> cancellableRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        // If we detect a CancellationException, we should not be trying to catch/handle it.
        throw c
    } catch (e: Exception) {
        Result.failure(e)
    }
}
