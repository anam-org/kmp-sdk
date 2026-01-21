package ai.anam.lab.client.core

import ai.anam.lab.client.core.navigation.FeatureContent
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.NavigationEvent
import ai.anam.lab.client.core.navigation.NavigationHost
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.ui.core.LocalPreferences
import ai.anam.lab.client.core.ui.theme.AnamTheme
import ai.anam.lab.client.core.viewmodel.LocalViewModelGraphProvider
import ai.anam.lab.client.core.viewmodel.ViewModelGraphProvider
import ai.anam.lab.client.feature.notifications.NotificationsView
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory

@Composable
fun App(
    features: Map<FeatureRoute, FeatureContent>,
    viewModelGraphProvider: ViewModelGraphProvider,
    preferences: AnamPreferences,
    imageLoader: ImageLoader,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    // Configure our ImageLoader for Coil.
    setSingletonImageLoaderFactory { imageLoader }

    val navController = rememberNavController()

    LaunchedEffect(navigator) {
        navigator.events.collect { event ->
            when (event) {
                is NavigationEvent.Navigate -> navController.navigate(event.route.route)
                is NavigationEvent.Pop -> navController.popBackStack()
            }
        }
    }

    CompositionLocalProvider(
        LocalViewModelGraphProvider provides viewModelGraphProvider,
        LocalPreferences provides preferences,
    ) {
        AnamTheme {
            Surface(modifier = modifier) {
                NavigationHost(
                    features = features,
                    navController = navController,
                )

                NotificationsView()
            }
        }
    }
}
