package ai.anam.lab.client.core

import ai.anam.lab.client.core.api.di.ApiSubgraph
import ai.anam.lab.client.core.client.di.ClientSubgraph
import ai.anam.lab.client.core.coroutines.CoroutinesSubgraph
import ai.anam.lab.client.core.http.di.HttpSubgraph
import ai.anam.lab.client.core.logging.di.LoggingSubgraph
import ai.anam.lab.client.core.navigation.FeatureContent
import ai.anam.lab.client.core.navigation.FeatureRoute
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
import ai.anam.lab.client.domain.data.di.DomainDataSubgraph
import ai.anam.lab.client.domain.notifications.di.DomainNotificationsSubgraph
import ai.anam.lab.client.domain.permissions.di.DomainPermissionsSubgraph
import ai.anam.lab.client.domain.session.di.DomainSessionSubgraph
import ai.anam.lab.client.feature.avatars.AvatarsFeatureViewSubgraph
import ai.anam.lab.client.feature.create.CreateAvatarFeatureViewSubgraph
import ai.anam.lab.client.feature.create.CreateAvatarScreen
import ai.anam.lab.client.feature.home.HomeFeatureViewSubgraph
import ai.anam.lab.client.feature.home.HomeScreen
import ai.anam.lab.client.feature.licenses.LicensesFeatureViewSubgraph
import ai.anam.lab.client.feature.licenses.LicensesScreen
import ai.anam.lab.client.feature.llms.LlmsFeatureViewSubgraph
import ai.anam.lab.client.feature.messages.MessagesFeatureViewSubgraph
import ai.anam.lab.client.feature.notifications.NotificationsFeatureViewSubgraph
import ai.anam.lab.client.feature.session.SessionFeatureViewSubgraph
import ai.anam.lab.client.feature.settings.SettingsFeatureViewSubgraph
import ai.anam.lab.client.feature.settings.SettingsScreen
import ai.anam.lab.client.feature.voices.VoicesFeatureViewSubgraph
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.Provides

/**
 * Shared manual bindings for iOS and wasmJs. This isn't ideal, but as of now, Metro can't support
 * building the Bindings automatically. We will have to pull them in ourselves until the upstream
 * issue is resolved.
 *
 * Reference: https://github.com/ZacSweers/metro/issues/460
 *
 * Platform-specific bindings (e.g. PlatformContext) are added by IosAppManualBindings and
 * WasmAppManualBindings in their respective source sets.
 *
 * Note: On Android, ViewModel bindings are provided via per-feature subgraphs using @ContributesTo.
 * On iOS/wasmJs, we extend those same subgraph interfaces here to inherit the bindings, since Metro
 * cannot auto-discover @ContributesTo contributions on those platforms.
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
    DomainPermissionsSubgraph,
    HomeFeatureViewSubgraph,
    AvatarsFeatureViewSubgraph,
    VoicesFeatureViewSubgraph,
    LlmsFeatureViewSubgraph,
    SettingsFeatureViewSubgraph,
    SessionFeatureViewSubgraph,
    MessagesFeatureViewSubgraph,
    NotificationsFeatureViewSubgraph,
    LicensesFeatureViewSubgraph,
    CreateAvatarFeatureViewSubgraph {

    @Binds
    fun AnamPreferencesImpl.bind(): AnamPreferences

    @Binds
    fun PermissionsManagerImpl.bind(): PermissionsManager

    @Binds
    fun NavigatorImpl.bind(): Navigator

    @Provides
    fun providesFeatures(): Map<FeatureRoute, FeatureContent> = mapOf(
        FeatureRoute.Home to { _ -> HomeScreen() },
        FeatureRoute.Settings to { _ -> SettingsScreen() },
        FeatureRoute.Licenses to { _ -> LicensesScreen() },
        FeatureRoute.Create to { _ -> CreateAvatarScreen() },
    )
}
