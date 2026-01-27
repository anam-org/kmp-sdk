package ai.anam.lab.client.core

import ai.anam.lab.PlatformContext
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.di.BaseApplicationObjectGraph
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.viewmodel.ViewModelGraphProvider
import coil3.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory

@DependencyGraph(AppScope::class)
actual interface ClientAppObjectGraph :
    BaseApplicationObjectGraph,
    HasClientViewObjectGraph,
    WasmAppManualBindings {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(): ClientAppObjectGraph
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
}

fun createClientAppObjectGraph(): ClientAppObjectGraph = createGraphFactory<ClientAppObjectGraph.Factory>()
    .create()
    .also { ApplicationObjectGraphHolder.set(it) }

/**
 * wasmJs-specific manual bindings. Extends [ClientAppManualBindings] with platform-specific
 * bindings (e.g. [PlatformContext]).
 */
interface WasmAppManualBindings : ClientAppManualBindings {

    @Provides
    fun providesPlatformContext(): PlatformContext = PlatformContext.INSTANCE
}
