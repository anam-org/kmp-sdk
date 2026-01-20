package ai.anam.lab.client.feature.llms

import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Llm
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.datetime.toFormattedDateString
import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.domain.data.FetchLlmsInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentLlmIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaLlmInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(LlmsViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class LlmsViewModel(
    private val fetchLlmsInteractor: FetchLlmsInteractor,
    private val observeCurrentLlmIdInteractor: ObserveCurrentLlmIdInteractor,
    private val setPersonaLlmInteractor: SetPersonaLlmInteractor,
    private val logger: Logger,
) : ViewModel() {

    val paginationState = PaginationState<Int, Llm>(
        initialPageKey = 1,
        onRequestPage = { loadPage(it) },
    )

    private val _state = MutableStateFlow(LlmsViewState(items = paginationState))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentLlmIdInteractor().collect { id ->
                _state.value = _state.value.copy(selectedId = id)
            }
        }
    }

    fun setLlm(id: String) {
        logger.i(TAG) { "Selecting LLM: $id" }
        _state.value = _state.value.copy(selectedId = id)
        viewModelScope.launch { setPersonaLlmInteractor(id) }
    }

    private fun loadPage(pageKey: Int) {
        logger.i(TAG) { "Loading Page: $pageKey" }

        viewModelScope.launch {
            fetchLlmsInteractor(
                page = pageKey,
                perPage = 10,
                query = null,
                includeDefaults = null,
            ).onLeft { error ->
                logger.e(TAG) { "Error loading LLMs: $error" }
                paginationState.setError(Exception(error.toString()))
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

    private companion object {
        const val TAG = "LlmsViewModel"
    }
}

data class LlmsViewState(val items: PaginationState<Int, Llm>, val selectedId: String? = null)

fun Llm.toSubtitle(): String {
    return listOfNotNull(llmFormat, modelName, createdAt.toFormattedDateString())
        .joinToString(" • ")
}
