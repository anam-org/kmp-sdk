package ai.anam.lab.client.core.ui.resources

import ai.anam.lab.client.core.ui.resources.generated.resources.CircularStd_Bold
import ai.anam.lab.client.core.ui.resources.generated.resources.CircularStd_Medium
import ai.anam.lab.client.core.ui.resources.generated.resources.GroteskRemix_Bold
import ai.anam.lab.client.core.ui.resources.generated.resources.GroteskRemix_Light
import ai.anam.lab.client.core.ui.resources.generated.resources.GroteskRemix_Medium
import ai.anam.lab.client.core.ui.resources.generated.resources.GroteskRemix_Regular
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font

val CircularFontFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.CircularStd_Medium, weight = FontWeight.Normal),
        Font(Res.font.CircularStd_Bold, weight = FontWeight.Bold),
    )

val GroteskRemixFontFamily: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.GroteskRemix_Regular, weight = FontWeight.Normal),
        Font(Res.font.GroteskRemix_Medium, weight = FontWeight.Medium),
        Font(Res.font.GroteskRemix_Light, weight = FontWeight.Light),
        Font(Res.font.GroteskRemix_Bold, weight = FontWeight.Bold),
    )
