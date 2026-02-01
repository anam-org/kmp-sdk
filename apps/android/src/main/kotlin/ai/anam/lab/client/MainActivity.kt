package ai.anam.lab.client

import ai.anam.lab.client.core.App
import ai.anam.lab.client.core.ClientAppObjectGraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    private val graph by lazy { ApplicationObjectGraphHolder.get<ClientAppObjectGraph>() }
    private val logger by lazy { graph.logger }

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.i(TAG) { "Activity created" }

        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                features = graph.features,
                viewModelGraphProvider = graph.viewModelGraphProvider,
                preferences = graph.preferences,
                imageLoader = graph.imageLoader,
                navigator = graph.navigator,
            )
        }
    }

    private companion object {
        const val TAG = "MainActivity"
    }
}
