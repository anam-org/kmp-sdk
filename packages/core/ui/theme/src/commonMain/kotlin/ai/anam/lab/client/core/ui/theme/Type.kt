package ai.anam.lab.client.core.ui.theme

import ai.anam.lab.client.core.ui.resources.CircularFontFamily
import ai.anam.lab.client.core.ui.resources.GroteskRemixFontFamily
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

val AnamTypography: Typography
    @Composable get() {
        val default = Typography()
        val defaultFontFamily = CircularFontFamily
        val titleFontFamily = GroteskRemixFontFamily
        return Typography(
            displayLarge = default.displayLarge.copy(fontFamily = defaultFontFamily),
            displayMedium = default.displayMedium.copy(fontFamily = defaultFontFamily),
            displaySmall = default.displaySmall.copy(fontFamily = defaultFontFamily),
            headlineLarge = default.headlineLarge.copy(fontFamily = titleFontFamily),
            headlineMedium = default.headlineMedium.copy(fontFamily = titleFontFamily),
            headlineSmall = default.headlineSmall.copy(fontFamily = titleFontFamily),
            titleLarge = default.titleLarge.copy(fontFamily = titleFontFamily),
            titleMedium = default.titleMedium.copy(fontFamily = titleFontFamily),
            titleSmall = default.titleSmall.copy(fontFamily = titleFontFamily),
            bodyLarge = default.bodyLarge.copy(fontFamily = defaultFontFamily),
            bodyMedium = default.bodyMedium.copy(fontFamily = defaultFontFamily),
            bodySmall = default.bodySmall.copy(fontFamily = defaultFontFamily),
            labelLarge = default.labelLarge.copy(fontFamily = defaultFontFamily),
            labelMedium = default.labelMedium.copy(fontFamily = defaultFontFamily),
            labelSmall = default.labelSmall.copy(fontFamily = defaultFontFamily),
        )
    }
