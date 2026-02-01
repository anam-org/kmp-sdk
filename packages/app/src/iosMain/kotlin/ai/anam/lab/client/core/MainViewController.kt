package ai.anam.lab.client.core

import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import androidx.compose.ui.window.ComposeUIViewController

/**
 * The main view controller for the iOS app.
 */
fun createMainViewController() = ComposeUIViewController {
    val graph = ApplicationObjectGraphHolder.get<ClientAppObjectGraph>()

    App(
        features = graph.features,
        preferences = graph.preferences,
        imageLoader = graph.imageLoader,
        viewModelGraphProvider = graph.viewModelGraphProvider,
        navigator = graph.navigator,
    )
}
