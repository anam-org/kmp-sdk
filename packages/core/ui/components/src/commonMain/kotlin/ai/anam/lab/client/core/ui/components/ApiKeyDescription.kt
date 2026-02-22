package ai.anam.lab.client.core.ui.components

import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.api_key_description
import ai.anam.lab.client.core.ui.resources.generated.resources.api_key_description_link
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import org.jetbrains.compose.resources.stringResource

private const val LAB_URL = "https://lab.anam.ai"

@Composable
fun ApiKeyDescription(
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    linkColor: Color = MaterialTheme.colorScheme.primary,
) {
    val linkText = stringResource(Res.string.api_key_description_link)
    val fullText = stringResource(Res.string.api_key_description, linkText)
    val linkStart = fullText.indexOf(linkText)

    val annotatedString = buildAnnotatedString {
        if (linkStart >= 0) {
            append(fullText.substring(0, linkStart))
            withLink(
                LinkAnnotation.Url(
                    url = LAB_URL,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline,
                        ),
                    ),
                ),
            ) {
                append(linkText)
            }
            append(fullText.substring(linkStart + linkText.length))
        } else {
            append(fullText)
        }
    }

    Text(
        text = annotatedString,
        style = textStyle,
        color = textColor,
        modifier = modifier,
    )
}
