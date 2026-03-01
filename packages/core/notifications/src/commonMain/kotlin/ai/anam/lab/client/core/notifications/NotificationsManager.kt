package ai.anam.lab.client.core.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Manages notifications throughout the application.
 * Provides a SharedFlow of notifications that can be observed by UI components.
 */
@Inject
@SingleIn(AppScope::class)
class NotificationsManager {

    /**
     * SharedFlow of notifications that can be observed by UI components.
     */
    val notifications: SharedFlow<Notification>
        field = MutableSharedFlow<Notification>(
            replay = 1,
            extraBufferCapacity = 1,
        )

    /**
     * Emits a notification to be displayed to the user.
     */
    suspend fun emit(notification: Notification) {
        notifications.emit(notification)
    }
}
