package ai.anam.lab.webrtc

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Platform-specific default dispatchers for common use cases.
 *
 * - [IO]: For I/O-like work (e.g. WebSocket, HTTP). On JVM/Android and iOS this is `kotlinx.coroutines.Dispatchers.IO`;
 *   on wasmJs it is `kotlinx.coroutines.Dispatchers.Main` since `Dispatchers.IO` is not available in the single-threaded JS environment.
 */
internal expect object DefaultDispatchers {
    val IO: CoroutineDispatcher
}
