package ai.anam.lab.client.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Platform-specific default dispatchers for common use cases.
 *
 * - [Main]: The main thread (e.g. UI). Maps to [kotlinx.coroutines.Dispatchers.Main] on all platforms.
 * - [Default]: The default dispatcher for CPU-bound work. Maps to [kotlinx.coroutines.Dispatchers.Default] on all platforms.
 * - [IO]: For I/O-like work (e.g. network, file I/O). On JVM/Android and iOS this is [kotlinx.coroutines.Dispatchers.IO];
 *   on wasmJs it is [kotlinx.coroutines.Dispatchers.Main] since [kotlinx.coroutines.Dispatchers.IO] is not available in the single-threaded JS environment.
 */
internal expect object DefaultDispatchers {
    val Main: CoroutineDispatcher
    val Default: CoroutineDispatcher
    val IO: CoroutineDispatcher
}
