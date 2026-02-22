package ai.anam.lab.client.feature.notifications

import ai.anam.lab.client.core.notifications.ErrorCode
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.error_sdk_error
import ai.anam.lab.client.core.ui.resources.generated.resources.notification_error_title
import ai.anam.lab.client.core.ui.resources.generated.resources.notification_info_title
import ai.anam.lab.client.core.ui.resources.generated.resources.notification_ok_button
import ai.anam.lab.client.core.ui.resources.generated.resources.notification_success_title
import ai.anam.lab.client.core.ui.resources.generated.resources.notification_warning_title
import ai.anam.lab.client.core.viewmodel.metroViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotificationsView(viewModel: NotificationsViewModel = metroViewModel()) {
    val viewState by viewModel.state.collectAsState()
    val currentNotification = viewState.currentNotification

    if (currentNotification != null) {
        NotificationDialog(
            notification = currentNotification,
            onDismiss = { viewModel.dismissNotification() },
        )
    }
}

@Composable
private fun NotificationDialog(notification: Notification, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (notification) {
                    is Notification.Error -> stringResource(Res.string.notification_error_title)
                    is Notification.Warning -> stringResource(Res.string.notification_warning_title)
                    is Notification.Info -> stringResource(Res.string.notification_info_title)
                    is Notification.Success -> stringResource(Res.string.notification_success_title)
                },
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Text(
                text = getNotificationMessage(notification),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.notification_ok_button))
            }
        },
    )
}

@Composable
private fun getNotificationMessage(notification: Notification): String {
    return when (notification) {
        is Notification.Error -> {
            when (notification.errorCode) {
                ErrorCode.SDK_ERROR -> notification.customMessage ?: stringResource(Res.string.error_sdk_error)
            }
        }
        is Notification.Warning -> notification.message
        is Notification.Info -> notification.message
        is Notification.Success -> notification.message
    }
}
