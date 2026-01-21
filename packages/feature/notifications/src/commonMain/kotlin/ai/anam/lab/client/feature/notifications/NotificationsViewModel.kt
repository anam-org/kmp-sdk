package ai.anam.lab.client.feature.notifications

import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.domain.notifications.ObserveNotificationsInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(NotificationsViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class NotificationsViewModel(
    private val observeNotificationsInteractor: ObserveNotificationsInteractor,
    private val logger: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeNotificationsInteractor().collect { notification ->
                logger.i(TAG) { "Received notification: ${notification.message}" }
                _state.value = _state.value.copy(currentNotification = notification)
            }
        }
    }

    fun dismissNotification() {
        logger.i(TAG) { "Dismissing notification" }
        _state.value = _state.value.copy(currentNotification = null)
    }

    private companion object {
        const val TAG = "NotificationsViewModel"
    }
}

data class NotificationsViewState(val currentNotification: Notification? = null)
