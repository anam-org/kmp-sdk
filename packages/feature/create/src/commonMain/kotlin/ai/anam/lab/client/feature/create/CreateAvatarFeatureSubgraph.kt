package ai.anam.lab.client.feature.create

import ai.anam.lab.client.core.navigation.FeatureContent
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.FeatureRouteKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface CreateAvatarFeatureSubgraph {

    @Provides
    @IntoMap
    @FeatureRouteKey(FeatureRoute.Create)
    fun providesCreateAvatarFeatureContent(): FeatureContent {
        return { _ -> CreateAvatarScreen() }
    }
}
