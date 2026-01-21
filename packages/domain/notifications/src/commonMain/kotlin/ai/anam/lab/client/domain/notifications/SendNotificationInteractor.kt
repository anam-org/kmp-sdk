package ai.anam.lab.client.domain.notifications

import ai.anam.lab.client.core.notifications.Notification

fun interface SendNotificationInteractor {
    suspend operator fun invoke(notification: Notification)
}
