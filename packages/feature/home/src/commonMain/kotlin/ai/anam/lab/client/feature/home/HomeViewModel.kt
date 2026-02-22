package ai.anam.lab.client.feature.home

import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_avatars
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_llms
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_messages
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_voices
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.IsApiKeyConfiguredInteractor
import ai.anam.lab.client.domain.data.ObserveApiKeyChangedInteractor
import ai.anam.lab.client.domain.data.UpdateApiKeyInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@Inject
class HomeViewModel(
    private val logger: Logger,
    private val navigator: Navigator,
    private val isApiKeyConfiguredInteractor: IsApiKeyConfiguredInteractor,
    private val updateApiKeyInteractor: UpdateApiKeyInteractor,
    private val observeApiKeyChangedInteractor: ObserveApiKeyChangedInteractor,
) : BaseViewModel<HomeViewState>(HomeViewState()) {

    private var checkApiKeyJob: Job? = null

    init {
        checkApiKey()

        viewModelScope.launch {
            observeApiKeyChangedInteractor().collect {
                checkApiKey()
            }
        }
    }

    private fun checkApiKey() {
        checkApiKeyJob?.cancel()
        checkApiKeyJob = viewModelScope.launch {
            val isConfigured = isApiKeyConfiguredInteractor()
            if (!isConfigured && !state.value.welcomeOverlayDismissed) {
                logger.i(TAG) { "API key not configured, showing welcome overlay" }
                setState { copy(showWelcomeOverlay = true) }
            } else {
                setState { copy(showWelcomeOverlay = false) }
            }
        }
    }

    fun dismissWelcomeOverlay() {
        logger.i(TAG) { "Dismissing welcome overlay" }
        setState { copy(showWelcomeOverlay = false, welcomeOverlayDismissed = true) }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            val changed = updateApiKeyInteractor(key)
            if (changed) {
                logger.i(TAG) { "API key updated" }
                setState { copy(showWelcomeOverlay = false, welcomeOverlayDismissed = true) }
            }
        }
    }

    fun selectSettings() {
        logger.i(TAG) { "Navigating to settings" }
        navigator.navigate(FeatureRoute.Settings)
    }

    fun selectTab(index: Int) {
        logger.i(TAG) { "Tab selected: $index" }
        setState { copy(selectedIndex = index) }
    }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}

data class HomeViewState(
    val selectedIndex: Int = 0,
    val tabs: List<Tab> = listOf(
        Tab.Avatar,
        Tab.Llms,
        Tab.Voices,
        Tab.Messages,
    ),
    val showWelcomeOverlay: Boolean = false,
    val welcomeOverlayDismissed: Boolean = false,
) : ViewState

sealed class Tab(val name: StringResource) {
    data object Avatar : Tab(Res.string.tab_avatars)
    data object Messages : Tab(Res.string.tab_messages)
    data object Llms : Tab(Res.string.tab_llms)
    data object Voices : Tab(Res.string.tab_voices)
}
