package ai.anam.lab.client.feature.messages

import ai.anam.lab.Message
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.session.ObserveActiveMessageHistoryInteractor
import ai.anam.lab.client.domain.session.ObserveIsSessionActiveInteractor
import ai.anam.lab.client.domain.session.SendActiveSessionUserMessageInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class MessagesViewModel(
    private val observeActiveMessageHistoryInteractor: ObserveActiveMessageHistoryInteractor,
    private val observeIsSessionActiveInteractor: ObserveIsSessionActiveInteractor,
    private val sendActiveSessionUserMessageInteractor: SendActiveSessionUserMessageInteractor,
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

    fun sendUserMessage(content: String) {
        viewModelScope.launch {
            runCatching { sendActiveSessionUserMessageInteractor(content) }
                .onFailure { e -> logger.w(tag = TAG, throwable = e, message = { "Failed to send user message" }) }
        }
    }

    private companion object {
        const val TAG = "MessagesViewModel"
    }
}

data class MessagesViewState(val messages: List<Message> = emptyList(), val isActive: Boolean = false) : ViewState
