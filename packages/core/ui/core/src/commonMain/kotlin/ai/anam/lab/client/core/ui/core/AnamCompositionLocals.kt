package ai.anam.lab.client.core.ui.core

import ai.anam.lab.client.core.settings.AnamPreferences
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPreferences = staticCompositionLocalOf<AnamPreferences> {
    error("LocalPreferences not provided")
}
