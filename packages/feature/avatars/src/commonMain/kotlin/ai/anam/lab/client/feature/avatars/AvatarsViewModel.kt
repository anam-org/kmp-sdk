package ai.anam.lab.client.feature.avatars

import ai.anam.lab.client.core.common.onLeft
import ai.anam.lab.client.core.common.onRight
import ai.anam.lab.client.core.data.models.Avatar
import ai.anam.lab.client.core.data.models.isLastPage
import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.domain.data.FetchAvatarsInteractor
import ai.anam.lab.client.domain.data.ObserveCurrentAvatarIdInteractor
import ai.anam.lab.client.domain.data.SetPersonaAvatarInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import io.github.ahmad_hamwi.compose.pagination.PaginationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(AvatarsViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class AvatarsViewModel(
    private val fetchAvatarsInteractor: FetchAvatarsInteractor,
    private val observeCurrentAvatarIdInteractor: ObserveCurrentAvatarIdInteractor,
    private val setPersonaAvatarInteractor: SetPersonaAvatarInteractor,
    private val logger: Logger,
) : ViewModel() {

    val paginationState = PaginationState<Int, Avatar>(
        initialPageKey = 1,
        onRequestPage = { loadPage(it) },
    )

    private val _state = MutableStateFlow(AvatarsViewState(items = paginationState))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentAvatarIdInteractor().collect { id ->
                _state.value = _state.value.copy(selectedId = id)
            }
        }
    }

    fun setAvatar(id: String, name: String) {
        logger.i(TAG) { "Selecting avatar: $id" }
        _state.value = _state.value.copy(selectedId = id)
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
        const val TAG = "AvatarsViewModel"
    }
}

data class AvatarsViewState(val items: PaginationState<Int, Avatar>, val selectedId: String? = null)
