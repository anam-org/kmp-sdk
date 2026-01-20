package ai.anam.lab.client.core.ui.theme

import ai.anam.lab.client.core.settings.Theme
import ai.anam.lab.client.core.ui.core.LocalPreferences
import ai.anam.lab.client.core.ui.core.collectAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AnamTheme(useDarkColors: Boolean = shouldUseDarkColors(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDarkColors) {
            AnamDarkColors
        } else {
            AnamLightColors
        },
        typography = AnamTypography,
        content = content,
    )
}

@Composable
fun shouldUseDarkColors(): Boolean {
    val themePreference = LocalPreferences.current.theme.collectAsState()
    return when (themePreference.value) {
        Theme.LIGHT -> false
        Theme.DARK -> true
        else -> isSystemInDarkTheme()
    }
}
