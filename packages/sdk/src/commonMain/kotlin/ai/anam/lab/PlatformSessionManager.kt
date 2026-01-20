package ai.anam.lab

import ai.anam.lab.utils.Logger

/**
 * Responsible for handling any platform specific resources while a [Session] is active.
 *
 * For example, on Android, this will deal with any required AudioFocus, ensuring that we restore the system
 * configuration on completed.
 */
internal interface PlatformSessionManager {
    /**
     * This function starts the management of the [Session] on the specific platform. When the session is completed, the
     * associated CoroutineScope will have been cancelled.
     */
    suspend fun start()
}

/**
 * Factory function to create a platform specific [PlatformSessionManager].
 */
internal expect fun createPlatformSessionManager(context: PlatformContext, logger: Logger): PlatformSessionManager
