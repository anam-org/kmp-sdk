package ai.anam.lab.client.domain.notifications.di

import ai.anam.lab.client.core.notifications.NotificationsManager
import ai.anam.lab.client.domain.notifications.ObserveNotificationsInteractor
import ai.anam.lab.client.domain.notifications.SendNotificationInteractor
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface DomainNotificationsSubgraph {

    @Provides
    fun providesObserveNotificationsInteractor(
        notificationsManager: NotificationsManager,
    ): ObserveNotificationsInteractor = ObserveNotificationsInteractor {
        notificationsManager.notifications
    }

    @Provides
    fun providesSendNotificationInteractor(notificationsManager: NotificationsManager): SendNotificationInteractor =
        SendNotificationInteractor { notification ->
            notificationsManager.emit(notification)
        }
}
