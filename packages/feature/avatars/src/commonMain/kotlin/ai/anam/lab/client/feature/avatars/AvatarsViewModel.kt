package ai.anam.lab.client.feature.avatars

import ai.anam.lab.client.core.common.NotAuthorizedException
import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.FetchAvatarsInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaAvatarInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Inject
class AvatarsViewModel(
    private val fetchAvatarsInteractor: FetchAvatarsInteractor,
    private val observeCurrentAvatarIdInteractor: ObserveCurrentAvatarIdInteractor,
    private val setPersonaAvatarInteractor: SetPersonaAvatarInteractor,
    private val observeApiKeyChangedInteractor: ObserveApiKeyChangedInteractor,
    private val logger: Logger,
) : BaseViewModel<AvatarsViewState>(
    AvatarsViewState(items = PaginationState(initialPageKey = 1, onRequestPage = {})),
) {

    private var paginationState = createPaginationState()
    private var searchJob: Job? = null

    init {
        setState { copy(items = paginationState) }

        viewModelScope.launch {
            observeCurrentAvatarIdInteractor().collect { id ->
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

    fun setAvatar(id: String, name: String) {
        logger.i(TAG) { "Selecting avatar: $id" }
        setState { copy(selectedId = id) }
        viewModelScope.launch { setPersonaAvatarInteractor(id, name) }
    }

    private fun loadPage(pageKey: Int) {
        logger.i(TAG) { "Loading Page: $pageKey" }

        viewModelScope.launch {
            fetchAvatarsInteractor(
                page = pageKey,
                perPage = 10,
                query = state.value.query.ifBlank { null },
                onlyOneShot = null,
            ).onLeft { error ->
                logger.e(TAG) { "Error loading avatars: $error" }
                // A 404 on the first page means no results matched the search query —
                // treat it as an empty result set so the pagination library shows the empty
                // indicator instead of an error. On later pages a 404 is unexpected, so
                // fall through to the normal error path.
                if (error is AvatarErrorReason.AvatarNotFound && pageKey == 1) {
                    paginationState.appendPage(
                        items = emptyList(),
                        nextPageKey = pageKey + 1,
                        isLastPage = true,
                    )
                } else {
                    val exception = when (error) {
                        is AvatarErrorReason.NotAuthorized -> NotAuthorizedException()
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

    private fun createPaginationState() = PaginationState<Int, Avatar>(
        initialPageKey = 1,
        onRequestPage = { loadPage(it) },
    )

    private fun resetPagination() {
        paginationState = createPaginationState()
        setState { copy(items = paginationState) }
    }

    private companion object {
        const val TAG = "AvatarsViewModel"
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

data class AvatarsViewState(
    val items: PaginationState<Int, Avatar>,
    val selectedId: String? = null,
    val query: String = "",
) : ViewState
