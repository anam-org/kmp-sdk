package ai.anam.lab.client.core.ui.imageloading.di

import ai.anam.lab.client.core.ui.imageloading.createImageLoader
import android.app.Application
import coil3.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
actual interface ImageLoadingSubgraph {

    @Provides
    fun providesImageLoader(application: Application): ImageLoader = createImageLoader(
        context = application,
    )
}
