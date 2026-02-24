package ai.anam.lab.client.feature.voices

import ai.anam.lab.client.core.common.NotAuthorizedException
import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.data.models.VoiceErrorReason
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.datetime.toFormattedDateString
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.FetchVoicesInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentVoiceIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaVoiceInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class VoicesViewModel(
    private val fetchVoicesInteractor: FetchVoicesInteractor,
    private val observeCurrentVoiceIdInteractor: ObserveCurrentVoiceIdInteractor,
    private val setPersonaVoiceInteractor: SetPersonaVoiceInteractor,
    private val observeApiKeyChangedInteractor: ObserveApiKeyChangedInteractor,
    private val logger: Logger,
) : BaseViewModel<VoicesViewState>(
    VoicesViewState(items = PaginationState(initialPageKey = 1, onRequestPage = {})),
) {

    private var paginationState = createPaginationState()
    private var searchJob: Job? = null

    init {
        setState { copy(items = paginationState) }

        viewModelScope.launch {
            observeCurrentVoiceIdInteractor().collect { id ->
                setState { copy(selectedId = id) }
            }
        }

        viewModelScope.launch {
            observeApiKeyChangedInteractor().collect {
                logger.i(TAG) { "API key changed, resetting pagination" }
                resetPagination()
            }
        }
    }

    fun onQueryChange(query: String) {
        setState { copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            resetPagination()
        }
    }

    fun setVoice(id: String) {
        logger.i(TAG) { "Selecting voice: $id" }
        setState { copy(selectedId = id) }
        viewModelScope.launch { setPersonaVoiceInteractor(id) }
    }

    private fun loadPage(pageKey: Int) {
        logger.i(TAG) { "Loading Page: $pageKey" }

        viewModelScope.launch {
            fetchVoicesInteractor(
                page = pageKey,
                perPage = 10,
                query = state.value.query.ifBlank { null },
            ).onLeft { error ->
                logger.e(TAG) { "Error loading voices: $error" }
                if (error is VoiceErrorReason.VoiceNotFound && pageKey == 1) {
                    paginationState.appendPage(
                        items = emptyList(),
                        nextPageKey = pageKey + 1,
                        isLastPage = true,
                    )
                } else {
                    val exception = when (error) {
                        is VoiceErrorReason.NotAuthorized -> NotAuthorizedException()
                        else -> Exception(error.toString())
                    }
                    paginationState.setError(exception)
                }
            }.onRight { page ->
                logger.i(TAG) { "Loaded new page (${page.data.size} items)" }
                paginationState.appendPage(
                    items = page.data,
                    nextPageKey = pageKey + 1,
                    isLastPage = page.meta.isLastPage(),
                )
            }
        }
    }

    private fun createPaginationState() = PaginationState<Int, Voice>(
        initialPageKey = 1,
        onRequestPage = { loadPage(it) },
    )

    private fun resetPagination() {
        paginationState = createPaginationState()
        setState { copy(items = paginationState) }
    }

    private companion object {
        const val TAG = "VoicesViewModel"
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

data class VoicesViewState(
    val items: PaginationState<Int, Voice>,
    val selectedId: String? = null,
    val query: String = "",
) : ViewState

fun Voice.toSubtitle(): String {
    return listOfNotNull(provider, gender, country, createdAt.toFormattedDateString())
        .joinToString(" • ")
}
