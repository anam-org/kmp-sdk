package ai.anam.lab

import ai.anam.lab.utils.Logger

internal actual fun createPlatformSessionManager(context: PlatformContext, logger: Logger): PlatformSessionManager {
    return PlatformSessionManagerImpl()
}

internal class PlatformSessionManagerImpl : PlatformSessionManager {
    override suspend fun start() {
        // No-op for wasmJs. Optional later: use Page Visibility API to adjust behavior when the tab is in the background.
    }
}
