package ai.anam.lab.client.core.ui.imageloading

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient

/**
 * Function to create a suitable [ImageLoader] for the current platform. This will use an in memory cache for images.
 */
internal fun createImageLoader(
    context: PlatformContext,
    maxMemoryCacheSizePercent: Double = 0.2,
    httpClient: () -> HttpClient = { HttpClient() },
): ImageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder().maxSizePercent(
            context = context,
            percent = maxMemoryCacheSizePercent,
        ).build()
    }
    .components {
        add(KtorNetworkFetcherFactory(httpClient = httpClient))
    }
    .build()
