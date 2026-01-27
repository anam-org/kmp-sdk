package ai.anam.lab.client.core.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual object DefaultDispatchers {
    actual val Main: CoroutineDispatcher
        get() = Dispatchers.Main
    actual val Default: CoroutineDispatcher
        get() = Dispatchers.Default
    actual val IO: CoroutineDispatcher
        get() = Dispatchers.IO
}
