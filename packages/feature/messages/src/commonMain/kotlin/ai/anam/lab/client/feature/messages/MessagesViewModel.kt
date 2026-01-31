package ai.anam.lab.client.feature.messages

import ai.anam.lab.Message
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.session.ObserveActiveMessageHistoryInteractor
import ai.anam.lab.client.domain.session.ObserveIsSessionActiveInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class MessagesViewModel(
    private val observeActiveMessageHistoryInteractor: ObserveActiveMessageHistoryInteractor,
    private val observeIsSessionActiveInteractor: ObserveIsSessionActiveInteractor,
    private val logger: Logger,
) : BaseViewModel<MessagesViewState>(MessagesViewState()) {

    init {
        viewModelScope.launch {
            observeActiveMessageHistoryInteractor().collect { messages ->
                setState { copy(messages = messages) }
            }
        }

        viewModelScope.launch {
            observeIsSessionActiveInteractor().collect { isActive ->
                logger.i(TAG) { "Session activity changed, isActive: $isActive" }
                setState { copy(isActive = isActive) }
            }
        }
    }

    private companion object {
        const val TAG = "MessagesViewModel"
    }
}

data class MessagesViewState(val messages: List<Message> = emptyList(), val isActive: Boolean = false) : ViewState
