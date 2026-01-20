package ai.anam.lab.client.feature.messages

import ai.anam.lab.Message
import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.domain.session.ObserveActiveMessageHistoryInteractor
import ai.anam.lab.client.domain.session.ObserveIsSessionActiveInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(MessagesViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class MessagesViewModel(
    private val observeActiveMessageHistoryInteractor: ObserveActiveMessageHistoryInteractor,
    private val observeIsSessionActiveInteractor: ObserveIsSessionActiveInteractor,
    private val logger: Logger,
) : ViewModel() {

    private val _state = MutableStateFlow(MessagesViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeActiveMessageHistoryInteractor().collect { messages ->
                _state.value = _state.value.copy(messages = messages)
            }
        }

        viewModelScope.launch {
            observeIsSessionActiveInteractor().collect { isActive ->
                logger.i(TAG) { "Session activity changed, isActive: $isActive" }
                _state.value = _state.value.copy(isActive = isActive)
            }
        }
    }

    private companion object {
        const val TAG = "MessagesViewModel"
    }
}

data class MessagesViewState(val messages: List<Message> = emptyList(), val isActive: Boolean = false)
