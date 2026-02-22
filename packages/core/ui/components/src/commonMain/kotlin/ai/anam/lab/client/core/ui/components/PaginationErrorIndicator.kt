package ai.anam.lab.client.core.ui.components

import ai.anam.lab.client.core.common.NotAuthorizedException
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.error_generic_title
import ai.anam.lab.client.core.ui.resources.generated.resources.error_not_authorized_title
import ai.anam.lab.client.core.ui.resources.generated.resources.error_retry_button
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Key
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

@Composable
fun PaginationErrorIndicator(exception: Throwable, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (exception is NotAuthorizedException) {
            Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.error_not_authorized_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ApiKeyDescription(
                textAlign = TextAlign.Center,
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.error_generic_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val message = exception.message
            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text(text = stringResource(Res.string.error_retry_button))
            }
        }
    }
}
