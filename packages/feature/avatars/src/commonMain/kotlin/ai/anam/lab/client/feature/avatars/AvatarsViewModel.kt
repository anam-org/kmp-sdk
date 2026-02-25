package ai.anam.lab.client.feature.avatars

import ai.anam.lab.client.core.common.NotAuthorizedException
import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.AvatarErrorReason
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.notifications.ErrorCode
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.avatars_delete_cancel_label
import ai.anam.lab.client.core.ui.resources.generated.resources.avatars_delete_confirm_label
import ai.anam.lab.client.core.ui.resources.generated.resources.avatars_delete_confirm_message
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.DeleteAvatarInteractor
import ai.anam.lab.client.domain.data.FetchAvatarsInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaAvatarInteractor
import ai.anam.lab.client.domain.notifications.SendNotificationInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.ExperimentalPaginationApi
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString

@Inject
class AvatarsViewModel(
    private val fetchAvatarsInteractor: FetchAvatarsInteractor,
    private val deleteAvatarInteractor: DeleteAvatarInteractor,
    private val observeCurrentAvatarIdInteractor: ObserveCurrentAvatarIdInteractor,
    private val setPersonaAvatarInteractor: SetPersonaAvatarInteractor,
    private val observeApiKeyChangedInteractor: ObserveApiKeyChangedInteractor,
    private val sendNotificationInteractor: SendNotificationInteractor,
    private val logger: Logger,
) : BaseViewModel<AvatarsViewState>(
    AvatarsViewState(items = PaginationState(initialPageKey = 1, onRequestPage = {})),
) {

    private var paginationState = createPaginationState()
    private var lastNextPageKey: Int = 2
    private var lastIsLastPage: Boolean = false
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

    fun onOneShotChange(enabled: Boolean) {
        setState { copy(onlyOneShot = enabled) }
        searchJob?.cancel()
        resetPagination()
    }

    fun resetFilters() {
        setState { copy(query = "", onlyOneShot = false) }
        searchJob?.cancel()
        resetPagination()
    }

    fun setAvatar(id: String, name: String) {
        logger.i(TAG) { "Selecting avatar: $id" }
        setState { copy(selectedId = id) }
        viewModelScope.launch { setPersonaAvatarInteractor(id, name) }
    }

    fun deleteAvatar(id: String) {
        logger.i(TAG) { "Requesting delete confirmation for avatar: $id" }
        viewModelScope.launch {
            sendNotificationInteractor(
                Notification.Confirmation(
                    message = getString(Res.string.avatars_delete_confirm_message),
                    confirmLabel = getString(Res.string.avatars_delete_confirm_label),
                    dismissLabel = getString(Res.string.avatars_delete_cancel_label),
                    onConfirm = { performDeleteAvatar(id) },
                ),
            )
        }
    }

    // After a successful delete, we can't simply remove the item from the local list because server-side pagination
    // indices shift by -1 — the next page load would skip an item at the boundary. Instead, we re-fetch all currently
    // loaded pages in a single request (page=1, perPage=pagesLoaded * PAGE_SIZE) to get a server-consistent list, then
    // replace the pagination data in place via appendPageWithUpdates. This preserves the user's scroll position while
    // keeping page boundaries aligned for subsequent page loads. If the re-fetch size exceeds the API's maximum page
    // size (MAX_PAGE_SIZE), we fall back to a full pagination reset.
    @OptIn(ExperimentalPaginationApi::class)
    private suspend fun performDeleteAvatar(id: String) {
        logger.i(TAG) { "Deleting avatar: $id" }
        deleteAvatarInteractor(id)
            .onRight {
                // Re-fetch everything we've loaded so far in one call to stay aligned with server pagination offsets.
                val pagesLoaded = lastNextPageKey - 1
                val refetchSize = pagesLoaded * PAGE_SIZE
                if (refetchSize > MAX_PAGE_SIZE) {
                    resetPagination()
                    return@onRight
                }
                fetchAvatarsInteractor(
                    page = 1,
                    perPage = refetchSize,
                    query = state.value.query.ifBlank { null },
                    onlyOneShot = state.value.onlyOneShot.takeIf { it },
                ).onRight { page ->
                    lastIsLastPage = page.meta.isLastPage()
                    paginationState.appendPageWithUpdates(
                        allItems = page.data,
                        nextPageKey = lastNextPageKey,
                        isLastPage = lastIsLastPage,
                    )
                }.onLeft {
                    // Re-fetch failed — fall back to a full reset.
                    resetPagination()
                }
            }
            .onLeft { error ->
                logger.e(TAG) { "Error deleting avatar: $error" }
                val message = when (error) {
                    is AvatarErrorReason.Unknown -> error.message
                    else -> error.toString()
                }
                sendNotificationInteractor(
                    Notification.Error(
                        errorCode = ErrorCode.API_ERROR,
                        customMessage = message,
                    ),
                )
            }
    }

    private fun loadPage(pageKey: Int) {
        logger.i(TAG) { "Loading Page: $pageKey" }

        viewModelScope.launch {
            fetchAvatarsInteractor(
                page = pageKey,
                perPage = PAGE_SIZE,
                query = state.value.query.ifBlank { null },
                onlyOneShot = state.value.onlyOneShot.takeIf { it },
            ).onLeft { error ->
                logger.e(TAG) { "Error loading avatars: $error" }
                // A 404 on the first page means no results matched the search query —
                // treat it as an empty result set so the pagination library shows the empty
                // indicator instead of an error. On later pages a 404 is unexpected, so
                // fall through to the normal error path.
                if (error is AvatarErrorReason.AvatarNotFound && pageKey == 1) {
                    lastNextPageKey = pageKey + 1
                    lastIsLastPage = true
                    paginationState.appendPage(
                        items = emptyList(),
                        nextPageKey = lastNextPageKey,
                        isLastPage = lastIsLastPage,
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
                lastNextPageKey = pageKey + 1
                lastIsLastPage = page.meta.isLastPage()
                paginationState.appendPage(
                    items = page.data,
                    nextPageKey = lastNextPageKey,
                    isLastPage = lastIsLastPage,
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
        lastNextPageKey = 2
        lastIsLastPage = false
        setState { copy(items = paginationState) }
    }

    private companion object {
        const val TAG = "AvatarsViewModel"
        const val PAGE_SIZE = 10
        const val MAX_PAGE_SIZE = 100
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}

data class AvatarsViewState(
    val items: PaginationState<Int, Avatar>,
    val selectedId: String? = null,
    val query: String = "",
    val onlyOneShot: Boolean = false,
) : ViewState
