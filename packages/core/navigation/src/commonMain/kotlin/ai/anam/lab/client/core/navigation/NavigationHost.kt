package ai.anam.lab.client.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// The Composable function that will create the content associated with the FeatureRoute.
typealias FeatureContent = @Composable (NavHostController) -> Unit

@Composable
fun NavigationHost(
    features: Map<FeatureRoute, FeatureContent>,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = FeatureRoute.Home.route,
    ) {
        features.forEach { (feature, content) ->
            composable(feature.route) { content(navController) }
        }
    }
}
