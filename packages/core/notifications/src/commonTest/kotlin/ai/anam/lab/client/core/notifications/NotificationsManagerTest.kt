package ai.anam.lab.client.core.notifications

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class NotificationsManagerTest {

    @Test
    fun `emit delivers notification to collector`() = runTest {
        val manager = NotificationsManager()

        manager.notifications.test {
            manager.emit(Notification.Info("Hello"))
            assertEquals("Hello", awaitItem().message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emit delivers multiple notifications in order`() = runTest {
        val manager = NotificationsManager()

        manager.notifications.test {
            manager.emit(Notification.Info("First"))
            manager.emit(Notification.Warning("Second"))

            assertEquals("First", awaitItem().message)
            assertEquals("Second", awaitItem().message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emit delivers different notification types`() = runTest {
        val manager = NotificationsManager()

        manager.notifications.test {
            manager.emit(Notification.Success("Done"))
            val item = awaitItem()
            assertIs<Notification.Success>(item)
            assertEquals("Done", item.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `notifications replays last emission to new collectors`() = runTest {
        val manager = NotificationsManager()

        manager.emit(Notification.Info("Replayed"))

        manager.notifications.test {
            assertEquals("Replayed", awaitItem().message)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
