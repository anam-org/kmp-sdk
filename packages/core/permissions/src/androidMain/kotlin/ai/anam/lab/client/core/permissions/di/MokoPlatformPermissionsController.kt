package ai.anam.lab.client.core.permissions.di

import ai.anam.lab.client.core.permissions.PermissionResult
import ai.anam.lab.client.core.permissions.PlatformPermissionsController
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.camera.CameraPermission
import dev.icerock.moko.permissions.microphone.RecordAudioPermission

internal class MokoPlatformPermissionsController(private val controller: PermissionsController) :
    PlatformPermissionsController {

    override val bindTarget: Any get() = controller

    override suspend fun requestRecordAudio(): PermissionResult = try {
        controller.providePermission(RecordAudioPermission)
        PermissionResult.Granted
    } catch (_: DeniedException) {
        PermissionResult.Denied
    } catch (_: DeniedAlwaysException) {
        PermissionResult.DeniedAlways
    }

    override suspend fun requestCamera(): PermissionResult = try {
        controller.providePermission(CameraPermission)
        PermissionResult.Granted
    } catch (_: DeniedException) {
        PermissionResult.Denied
    } catch (_: DeniedAlwaysException) {
        PermissionResult.DeniedAlways
    }
}
