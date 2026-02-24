package ai.anam.lab.client.core.ui.components

import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.empty_search_description
import ai.anam.lab.client.core.ui.resources.generated.resources.empty_search_reset
import ai.anam.lab.client.core.ui.resources.generated.resources.empty_search_title
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource

/**
 * An empty-state indicator for paginated lists, intended for use as a `firstPageEmptyIndicator`. Displays a search-off
 * icon, a title, a description, and a button. All text is customisable; the defaults are search-oriented ("No results
 * found" / "Try a different search term" / "Reset").
 */
@Composable
fun PaginationEmptySearchIndicator(
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(Res.string.empty_search_title),
    description: String = stringResource(Res.string.empty_search_description),
    buttonLabel: String = stringResource(Res.string.empty_search_reset),
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onReset) {
            Text(text = buttonLabel)
        }
    }
}
