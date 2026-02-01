package ai.anam.lab.client.core

import ai.anam.lab.client.core.di.BaseApplicationObjectGraph
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.FeatureContent
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.viewmodel.ViewModelGraphProvider
import ai.anam.lab.client.feature.home.HomeScreen
import ai.anam.lab.client.feature.licenses.LicensesScreen
import ai.anam.lab.client.feature.settings.SettingsScreen
import android.app.Application
import android.content.Context
import coil3.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides

@DependencyGraph(AppScope::class)
actual interface ClientAppObjectGraph :
    BaseApplicationObjectGraph,
    HasClientViewObjectGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): ClientAppObjectGraph
    }

    /**
     * This factory is used to create new instances of [ClientViewObjectGraph] that will inherit AppScope based
     * dependencies. See [HasClientViewObjectGraph] for more information.
     */
    override val clientViewObjectGraphFactory: ClientViewObjectGraph.Factory

    val navigator: Navigator
    val logger: Logger
    val viewModelGraphProvider: ViewModelGraphProvider
    val preferences: AnamPreferences
    val imageLoader: ImageLoader
    val features: Map<FeatureRoute, FeatureContent>

    @Provides
    fun providesContext(application: Application): Context = application

    @Provides
    fun providesFeatures(): Map<FeatureRoute, FeatureContent> = mapOf(
        FeatureRoute.Home to { _ -> HomeScreen() },
        FeatureRoute.Settings to { _ -> SettingsScreen() },
        FeatureRoute.Licenses to { _ -> LicensesScreen() },
    )
}
