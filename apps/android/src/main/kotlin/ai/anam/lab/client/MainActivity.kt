package ai.anam.lab.client

import ai.anam.lab.client.core.App
import ai.anam.lab.client.core.ClientAppObjectGraph
import ai.anam.lab.client.core.di.ApplicationObjectGraphHolder
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.feature.home.HomeScreen
import ai.anam.lab.client.feature.licenses.LicensesScreen
import ai.anam.lab.client.feature.settings.SettingsScreen
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
                // TODO: Need to work out why this doesn't work when being built via the DI graph.
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

    private companion object {
        const val TAG = "MainActivity"
    }
}
