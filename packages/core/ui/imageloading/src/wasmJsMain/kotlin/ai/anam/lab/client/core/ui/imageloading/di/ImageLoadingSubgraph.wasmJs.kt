package ai.anam.lab.client.core.ui.imageloading.di

import ai.anam.lab.client.core.ui.imageloading.createImageLoader
import coil3.ImageLoader
import coil3.PlatformContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
actual interface ImageLoadingSubgraph {

    @Provides
    fun providesImageLoader(): ImageLoader = createImageLoader(
        context = PlatformContext.INSTANCE,
    )
}
