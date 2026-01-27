package ai.anam.lab.webrtc

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal actual object DefaultDispatchers {
    actual val IO: CoroutineDispatcher
        get() = Dispatchers.IO
}
