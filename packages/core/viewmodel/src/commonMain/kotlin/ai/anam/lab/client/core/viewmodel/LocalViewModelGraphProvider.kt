package ai.anam.lab.client.core.viewmodel

import ai.anam.lab.client.core.di.ViewModelGraph
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides access to the currently configured [ViewModelGraphProvider]. This is required to allow us to build new
 * [ViewModelGraph] instances when required.
 */
val LocalViewModelGraphProvider = staticCompositionLocalOf<ViewModelGraphProvider> {
    error("No ViewModelGraphProvider provided")
}
