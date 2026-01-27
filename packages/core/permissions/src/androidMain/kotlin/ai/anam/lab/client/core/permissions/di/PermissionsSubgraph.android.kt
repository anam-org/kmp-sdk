package ai.anam.lab.client.core.permissions.di

import ai.anam.lab.client.core.permissions.PlatformPermissionsController
import android.content.Context
import dev.icerock.moko.permissions.PermissionsController
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
actual interface PermissionsSubgraph {

    @SingleIn(AppScope::class)
    @Provides
    fun providesPermissionsController(context: Context): PermissionsController = PermissionsController(
        applicationContext = context,
    )

    @Provides
    fun providesPlatformPermissionsController(controller: PermissionsController): PlatformPermissionsController =
        MokoPlatformPermissionsController(controller)
}
