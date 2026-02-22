package ai.anam.lab.client.core.navigation

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// Shared transition keys for elements that animate across navigation destinations.
object SharedTransitionKeys {
    const val TOP_BAR = "topBar"
}

// Provided by [NavigationHost] via SharedTransitionLayout wrapping the NavHost.
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

// Provided by [NavigationHost] for each composable destination in the NavHost.
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Applies [SharedTransitionScope.sharedBounds] when shared transition scopes are available,
 * allowing elements with the same [key] across different navigation destinations to animate
 * smoothly between each other. Returns the modifier unchanged if scopes are not provided
 * (e.g. in previews or outside NavigationHost).
 *
 * On wasmJs, shared element transitions are not supported at runtime, so this is a no-op.
 */
@Composable
expect fun Modifier.sharedBoundsIfAvailable(key: String): Modifier
