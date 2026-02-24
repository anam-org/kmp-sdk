package ai.anam.lab.client.feature.llms

import ai.anam.lab.client.core.common.NotAuthorizedException
import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.data.models.LlmErrorReason
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.datetime.toFormattedDateString
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.FetchLlmsInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentLlmIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaLlmInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class LlmsViewModel(
    private val fetchLlmsInteractor: FetchLlmsInteractor,
    private val observeCurrentLlmIdInteractor: ObserveCurrentLlmIdInteractor,
    private val setPersonaLlmInteractor: SetPersonaLlmInteractor,
    private val observeApiKeyChangedInteractor: ObserveApiKeyChangedInteractor,
    private val logger: Logger,
) : BaseViewModel<LlmsViewState>(
    LlmsViewState(items = PaginationState(initialPageKey = 1, onRequestPage = {})),
) {

    private var paginationState = createPaginationState()
    private var searchJob: Job? = null

    init {
        setState { copy(items = paginationState) }

        viewModelScope.launch {
            observeCurrentLlmIdInteractor().collect { id ->
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

    fun setLlm(id: String) {
        logger.i(TAG) { "Selecting LLM: $id" }
        setState { copy(selectedId = id) }
        viewModelScope.launch { setPersonaLlmInteractor(id) }
    }

    private fun loadPage(pageKey: Int) {
        logger.i(TAG) { "Loading Page: $pageKey" }

        viewModelScope.launch {
            fetchLlmsInteractor(
                page = pageKey,
                perPage = 10,
                query = state.value.query.ifBlank { null },
                includeDefaults = null,
            ).onLeft { error ->
                logger.e(TAG) { "Error loading LLMs: $error" }
                if (error is LlmErrorReason.LlmNotFound && pageKey == 1) {
                    paginationState.appendPage(
                        items = emptyList(),
                        nextPageKey = pageKey + 1,
                        isLastPage = true,
                    )
                } else {
                    val exception = when (error) {
                        is LlmErrorReason.NotAuthorized -> NotAuthorizedException()
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

    private fun createPaginationState() = PaginationState<Int, Llm>(
        initialPageKey = 1,
        onRequestPage = { loadPage(it) },
    )

    private fun resetPagination() {
        paginationState = createPaginationState()
        setState { copy(items = paginationState) }
    }

    private companion object {
        const val TAG = "LlmsViewModel"
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

data class LlmsViewState(
    val items: PaginationState<Int, Llm>,
    val selectedId: String? = null,
    val query: String = "",
) : ViewState

fun Llm.toSubtitle(): String {
    return listOfNotNull(llmFormat, modelName, createdAt.toFormattedDateString())
        .joinToString(" • ")
}
