package ai.anam.lab.client.core

import ai.anam.lab.PlatformContext
import ai.anam.lab.client.core.api.di.ApiSubgraph
import ai.anam.lab.client.core.client.di.ClientSubgraph
import ai.anam.lab.client.core.coroutines.CoroutinesSubgraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.di.BaseApplicationObjectGraph
import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.http.di.HttpSubgraph
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.logging.di.LoggingSubgraph
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.navigation.NavigatorImpl
import ai.anam.lab.client.core.notifications.di.NotificationsSubgraph
import ai.anam.lab.client.core.permissions.PermissionsManager
import ai.anam.lab.client.core.permissions.PermissionsManagerImpl
import ai.anam.lab.client.core.permissions.di.PermissionsSubgraph
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.settings.AnamPreferencesImpl
import ai.anam.lab.client.core.settings.di.SettingsSubgraph
import ai.anam.lab.client.core.ui.imageloading.di.ImageLoadingSubgraph
import ai.anam.lab.client.core.viewmodel.ViewModelGraphProvider
import ai.anam.lab.client.domain.data.di.DomainDataSubgraph
import ai.anam.lab.client.domain.notifications.di.DomainNotificationsSubgraph
import ai.anam.lab.client.domain.permissions.di.DomainPermissionsSubgraph
import ai.anam.lab.client.domain.session.di.DomainSessionSubgraph
import ai.anam.lab.client.feature.avatars.AvatarsViewModel
import ai.anam.lab.client.feature.home.HomeViewModel
import ai.anam.lab.client.feature.licenses.LicensesViewModel
import ai.anam.lab.client.feature.llms.LlmsViewModel
import ai.anam.lab.client.feature.messages.MessagesViewModel
import ai.anam.lab.client.feature.notifications.NotificationsViewModel
import ai.anam.lab.client.feature.session.SessionViewModel
import ai.anam.lab.client.feature.settings.SettingsViewModel
import ai.anam.lab.client.feature.voices.VoicesViewModel
import androidx.lifecycle.ViewModel
import coil3.ImageLoader
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory

@DependencyGraph(AppScope::class)
actual interface ClientAppObjectGraph :
    BaseApplicationObjectGraph,
    HasClientViewObjectGraph,
    ClientAppManualBindings {
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
 * This isn't ideal, but as of now, Metro can't support building the Bindings automatically. We will have to pull them
 * in ourselves until the upstream issue is resolved.
 *
 * Reference: https://github.com/ZacSweers/metro/issues/460
 */
interface ClientAppManualBindings :
    LoggingSubgraph,
    CoroutinesSubgraph,
    SettingsSubgraph,
    ClientSubgraph,
    ImageLoadingSubgraph,
    PermissionsSubgraph,
    HttpSubgraph,
    ApiSubgraph,
    NotificationsSubgraph,
    DomainDataSubgraph,
    DomainNotificationsSubgraph,
    DomainSessionSubgraph,
    DomainPermissionsSubgraph {

    @Binds
    fun AnamPreferencesImpl.bind(): AnamPreferences

    @Binds
    fun PermissionsManagerImpl.bind(): PermissionsManager

    @Binds
    fun NavigatorImpl.bind(): Navigator

    @Provides
    fun providesPlatformContext(): PlatformContext = PlatformContext.INSTANCE

    @Provides
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun providesHomeViewModel(provider: Provider<HomeViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(SessionViewModel::class)
    fun providesSessionViewModel(provider: Provider<SessionViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(MessagesViewModel::class)
    fun providesMessagesViewModel(provider: Provider<MessagesViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(AvatarsViewModel::class)
    fun providesAvatarsViewModel(provider: Provider<AvatarsViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(VoicesViewModel::class)
    fun providesVoicesViewModel(provider: Provider<VoicesViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(LlmsViewModel::class)
    fun providesLlmsViewModel(provider: Provider<LlmsViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun providesSettingsViewModel(provider: Provider<SettingsViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(LicensesViewModel::class)
    fun providesLicensesViewModel(provider: Provider<LicensesViewModel>): ViewModel = provider()

    @Provides
    @IntoMap
    @ViewModelKey(NotificationsViewModel::class)
    fun providesNotificationsViewModel(provider: Provider<NotificationsViewModel>): ViewModel = provider()
}
