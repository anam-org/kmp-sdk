package ai.anam.lab.client.web

import ai.anam.lab.client.core.App
import ai.anam.lab.client.core.ClientAppObjectGraph
import ai.anam.lab.client.core.createClientAppObjectGraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.feature.home.HomeScreen
import ai.anam.lab.client.feature.licenses.LicensesScreen
import ai.anam.lab.client.feature.settings.SettingsScreen
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    createClientAppObjectGraph()

    ComposeViewport(viewportContainerId = "ComposeTarget") {
        val graph = ApplicationObjectGraphHolder.get<ClientAppObjectGraph>()
        App(
            features = mapOf(
                FeatureRoute.Home to { HomeScreen() },
                FeatureRoute.Settings to { SettingsScreen() },
                FeatureRoute.Licenses to { LicensesScreen() },
            ),
            viewModelGraphProvider = graph.viewModelGraphProvider,
            preferences = graph.preferences,
            imageLoader = graph.imageLoader,
            navigator = graph.navigator,
        )
    }
}
