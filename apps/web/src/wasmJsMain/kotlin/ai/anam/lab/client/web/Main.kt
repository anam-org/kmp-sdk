package ai.anam.lab.client.web

import ai.anam.lab.client.core.App
import ai.anam.lab.client.core.ClientAppObjectGraph
import ai.anam.lab.client.core.createClientAppObjectGraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    createClientAppObjectGraph()

    ComposeViewport(viewportContainerId = "ComposeTarget") {
        val graph = ApplicationObjectGraphHolder.get<ClientAppObjectGraph>()
        App(
            features = graph.features,
            viewModelGraphProvider = graph.viewModelGraphProvider,
            preferences = graph.preferences,
            imageLoader = graph.imageLoader,
            navigator = graph.navigator,
        )
    }
}
