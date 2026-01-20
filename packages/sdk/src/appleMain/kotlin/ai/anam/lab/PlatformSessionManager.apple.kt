package ai.anam.lab

import ai.anam.lab.utils.Logger

internal actual fun createPlatformSessionManager(context: PlatformContext, logger: Logger): PlatformSessionManager {
    return PlatformSessionManagerImpl()
}

internal class PlatformSessionManagerImpl : PlatformSessionManager {
    override suspend fun start() {
        // Nothing to do.
    }
}
