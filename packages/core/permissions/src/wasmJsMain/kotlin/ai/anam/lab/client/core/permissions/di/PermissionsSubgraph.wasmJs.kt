package ai.anam.lab.client.core.permissions.di

import ai.anam.lab.client.core.permissions.PlatformPermissionsController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
actual interface PermissionsSubgraph {

    @Provides
    fun providesPlatformPermissionsController(): PlatformPermissionsController = BrowserPlatformPermissionsController()
}
