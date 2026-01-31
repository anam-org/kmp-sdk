package ai.anam.lab.client.feature.notifications

import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.notifications.ObserveNotificationsInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class NotificationsViewModel(
    private val observeNotificationsInteractor: ObserveNotificationsInteractor,
    private val logger: Logger,
) : BaseViewModel<NotificationsViewState>(NotificationsViewState()) {

    init {
        viewModelScope.launch {
            observeNotificationsInteractor().collect { notification ->
                logger.i(TAG) { "Received notification: ${notification.message}" }
                setState { copy(currentNotification = notification) }
            }
        }
    }

    fun dismissNotification() {
        logger.i(TAG) { "Dismissing notification" }
        setState { copy(currentNotification = null) }
    }

    private companion object {
        const val TAG = "NotificationsViewModel"
    }
}

data class NotificationsViewState(val currentNotification: Notification? = null) : ViewState
