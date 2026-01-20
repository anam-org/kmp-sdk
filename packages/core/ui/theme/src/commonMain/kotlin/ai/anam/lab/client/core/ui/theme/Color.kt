package ai.anam.lab.client.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val AnamLightColors = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = Color(0xFFF5F5F5), // (245, 245, 245)
    onSecondary = Color.Black,
    surface = Color(0xFFFBFBFB), // (251, 251, 251),
    surfaceContainer = Color.White,
)

val AnamDarkColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFF262626), // (38, 38, 38)
    onSecondary = Color.White,
    surface = Color(0xFF0F0F0F), // (15, 15, 15)
    surfaceContainer = Color.Black,
)
