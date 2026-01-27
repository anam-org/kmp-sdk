package ai.anam.lab.client.core.client.di

import ai.anam.lab.PlatformContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
actual interface ClientPlatformContextSubgraph {

    @Provides
    fun providesPlatformContext(): PlatformContext = PlatformContext.INSTANCE
}
