package ai.anam.lab.fakes

import ai.anam.lab.PlatformSessionManager
import kotlinx.coroutines.awaitCancellation

class FakePlatformSessionManager : PlatformSessionManager {
    var isStarted: Boolean = false

    override suspend fun start() {
        try {
            isStarted = true
            awaitCancellation()
        } finally {
            isStarted = false
        }
    }
}
