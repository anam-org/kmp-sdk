package ai.anam.lab.client.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// The Composable function that will create the content associated with the FeatureRoute.
typealias FeatureContent = @Composable (NavHostController) -> Unit

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationHost(
    features: Map<FeatureRoute, FeatureContent>,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    SharedTransitionLayout {
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = FeatureRoute.Home.route,
            popEnterTransition = {
                if (isTransitionEnabled()) {
                    fadeIn(tween(DEFAULT_NAV_TRANSITION_MS))
                } else {
                    EnterTransition.None
                }
            },
            popExitTransition = {
                if (isTransitionEnabled()) {
                    fadeOut(tween(DEFAULT_NAV_TRANSITION_MS))
                } else {
                    ExitTransition.None
                }
            },
        ) {
            features.forEach { (feature, content) ->
                composable(feature.route) {
                    CompositionLocalProvider(
                        LocalSharedTransitionScope provides this@SharedTransitionLayout,
                        LocalAnimatedVisibilityScope provides this@composable,
                    ) {
                        content(navController)
                    }
                }
            }
        }
    }
}

// Matches the NavHost internal default (androidx.navigation.compose.NavHostKt.DefaultDurationMillis).
private const val DEFAULT_NAV_TRANSITION_MS = 700

// Pop transitions are disabled for routes that use SurfaceView (e.g. CameraX camera preview).
// SurfaceView renders on a separate window layer outside Compose's rendering pipeline, causing
// it to remain visible during opacity-based transition animations. Forward transitions keep the
// default fade because the destination defers SurfaceView creation until the transition completes.
private fun AnimatedContentTransitionScope<NavBackStackEntry>.isTransitionEnabled(): Boolean {
    val route = FeatureRoute.Create.route
    return initialState.destination.route != route && targetState.destination.route != route
}
