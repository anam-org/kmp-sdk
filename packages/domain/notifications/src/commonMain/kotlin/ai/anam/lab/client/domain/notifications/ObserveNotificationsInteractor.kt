package ai.anam.lab.client.domain.notifications

import ai.anam.lab.client.core.notifications.Notification
import kotlinx.coroutines.flow.Flow

fun interface ObserveNotificationsInteractor {
    suspend operator fun invoke(): Flow<Notification>
}
