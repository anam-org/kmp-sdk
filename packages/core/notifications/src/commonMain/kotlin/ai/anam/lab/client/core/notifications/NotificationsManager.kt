package ai.anam.lab.client.core.notifications

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manages notifications throughout the application.
 * Provides a Flow of notifications that can be observed by UI components.
 */
@Inject
@SingleIn(AppScope::class)
class NotificationsManager {

    private val _notifications = MutableSharedFlow<Notification>(
        replay = 1,
        extraBufferCapacity = 1,
    )

    /**
     * Flow of notifications that can be observed by UI components.
     */
    val notifications: Flow<Notification> = _notifications.asSharedFlow()

    /**
     * Emits a notification to be displayed to the user.
     */
    suspend fun emit(notification: Notification) {
        _notifications.emit(notification)
    }
}
