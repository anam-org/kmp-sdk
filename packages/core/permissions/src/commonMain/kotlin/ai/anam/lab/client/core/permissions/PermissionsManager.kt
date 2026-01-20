package ai.anam.lab.client.core.permissions

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.microphone.RECORD_AUDIO
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

interface PermissionsManager {
    val controller: PermissionsController

    suspend fun provideAudioPermission(): PermissionResult
}

enum class PermissionResult { Granted, Denied, DeniedAlways }

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PermissionsManagerImpl(override val controller: PermissionsController) : PermissionsManager {

    /**
     * Attempt to request the audio permission, and return the status of the result.
     */
    override suspend fun provideAudioPermission(): PermissionResult {
        return try {
            controller.providePermission(Permission.RECORD_AUDIO)
            PermissionResult.Granted
        } catch (_: DeniedException) {
            PermissionResult.Denied
        } catch (_: DeniedAlwaysException) {
            PermissionResult.DeniedAlways
        }
    }
}
