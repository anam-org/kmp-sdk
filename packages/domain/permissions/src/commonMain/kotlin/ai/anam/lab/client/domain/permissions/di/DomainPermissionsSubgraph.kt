package ai.anam.lab.client.domain.permissions.di

import ai.anam.lab.client.core.permissions.PermissionsManager
import ai.anam.lab.client.domain.permissions.RequestAudioPermissionInteractor
import ai.anam.lab.client.domain.permissions.RequestCameraPermissionInteractor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface DomainPermissionsSubgraph {

    @Provides
    fun providesRequestAudioPermissionInteractor(
        permissionsManager: PermissionsManager,
    ): RequestAudioPermissionInteractor {
        return RequestAudioPermissionInteractor { permissionsManager.provideAudioPermission() }
    }

    @Provides
    fun providesRequestCameraPermissionInteractor(
        permissionsManager: PermissionsManager,
    ): RequestCameraPermissionInteractor {
        return RequestCameraPermissionInteractor { permissionsManager.provideCameraPermission() }
    }
}
