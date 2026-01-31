package ai.anam.lab.client.feature.voices

import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Voice
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.datetime.toFormattedDateString
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.FetchVoicesInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentVoiceIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaVoiceInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.launch

@Inject
class VoicesViewModel(
    private val fetchVoicesInteractor: FetchVoicesInteractor,
    private val observeCurrentVoiceIdInteractor: ObserveCurrentVoiceIdInteractor,
    private val setPersonaVoiceInteractor: SetPersonaVoiceInteractor,
    private val logger: Logger,
) : BaseViewModel<VoicesViewState>(
    VoicesViewState(items = PaginationState(initialPageKey = 1, onRequestPage = {})),
) {

    val paginationState = PaginationState<Int, Voice>(
        initialPageKey = 1,
        onRequestPage = { loadPage(it) },
    )

    init {
        setState { copy(items = paginationState) }
        viewModelScope.launch {
            observeCurrentVoiceIdInteractor().collect { id ->
                setState { copy(selectedId = id) }
            }
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
                query = null,
            ).onLeft { error ->
                logger.e(TAG) { "Error loading voices: $error" }
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
        const val TAG = "VoicesViewModel"
    }
}

data class VoicesViewState(val items: PaginationState<Int, Voice>, val selectedId: String? = null) : ViewState

fun Voice.toSubtitle(): String {
    return listOfNotNull(provider, gender, country, createdAt.toFormattedDateString())
        .joinToString(" • ")
}
