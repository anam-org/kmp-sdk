package ai.anam.lab.client.core

import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.feature.home.HomeScreen
import ai.anam.lab.client.feature.licenses.LicensesScreen
import ai.anam.lab.client.feature.settings.SettingsScreen
import androidx.compose.ui.window.ComposeUIViewController

/**
 * The main view controller for the iOS app.
 */
fun createMainViewController() = ComposeUIViewController {
    val graph = ApplicationObjectGraphHolder.get<ClientAppObjectGraph>()

    App(
        features = mapOf(
            FeatureRoute.Home to { HomeScreen() },
            FeatureRoute.Settings to { SettingsScreen() },
            FeatureRoute.Licenses to { LicensesScreen() },
        ),
        preferences = graph.preferences,
        imageLoader = graph.imageLoader,
        viewModelGraphProvider = graph.viewModelGraphProvider,
        navigator = graph.navigator,
    )
}
