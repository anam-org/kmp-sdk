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
                query = null,
                onlyOneShot = null,
            ).onLeft { error ->
                logger.e(TAG) { "Error loading avatars: $error" }
                val exception = when (error) {
                    is AvatarErrorReason.NotAuthorized -> NotAuthorizedException()
                    else -> Exception(error.toString())
                }
                paginationState.setError(exception)
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
    }
}

data class AvatarsViewState(val items: PaginationState<Int, Avatar>, val selectedId: String? = null) : ViewState
