package ai.anam.lab.client.core.client.di

import ai.anam.lab.AnamClient
import ai.anam.lab.AnamClientOptions
import ai.anam.lab.PlatformContext
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface ClientSubgraph {
    @Provides
    @SingleIn(AppScope::class)
    fun providesAnamClient(context: PlatformContext): AnamClient = AnamClient(
        options = AnamClientOptions(
            context = context,
        ),
    )
}

/**
 * The [AnamClientOptions] require a [PlatformContext], which will be obtained via this Subgraph.
 */
expect interface ClientPlatformContextSubgraph
