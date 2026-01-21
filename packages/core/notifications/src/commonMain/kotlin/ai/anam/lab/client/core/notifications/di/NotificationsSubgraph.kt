package ai.anam.lab.client.core.notifications.di

import ai.anam.lab.client.core.notifications.NotificationsManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface NotificationsSubgraph {
    @Provides
    @SingleIn(AppScope::class)
    fun providesNotificationsManager(): NotificationsManager = NotificationsManager()
}
