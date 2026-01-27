package ai.anam.lab.client.core.permissions

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

interface PermissionsManager {
    fun getBindTarget(): Any?

    suspend fun provideAudioPermission(): PermissionResult
}

enum class PermissionResult { Granted, Denied, DeniedAlways }

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class PermissionsManagerImpl(private val controller: PlatformPermissionsController) : PermissionsManager {

    override fun getBindTarget(): Any? = controller.bindTarget

    /**
     * Attempt to request the audio permission, and return the status of the result.
     */
    override suspend fun provideAudioPermission(): PermissionResult = controller.requestRecordAudio()
}
