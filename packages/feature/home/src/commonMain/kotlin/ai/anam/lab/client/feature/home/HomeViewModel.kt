package ai.anam.lab.client.feature.home

import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.notifications.ErrorCode
import ai.anam.lab.client.core.notifications.Notification
import ai.anam.lab.client.core.ui.resources.generated.resources.Res
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_avatars
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_llms
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_messages
import ai.anam.lab.client.core.ui.resources.generated.resources.tab_voices
import ai.anam.lab.client.domain.data.IsApiKeyConfiguredInteractor
import ai.anam.lab.client.domain.notifications.SendNotificationInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

@Inject
@ViewModelKey(HomeViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class HomeViewModel(
    private val logger: Logger,
    private val navigator: Navigator,
    private val isApiKeyConfiguredInteractor: IsApiKeyConfiguredInteractor,
    private val sendNotificationInteractor: SendNotificationInteractor,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val isConfigured = isApiKeyConfiguredInteractor()
            if (!isConfigured) {
                logger.e(TAG) { "API key not configured" }
                sendNotificationInteractor(
                    Notification.Error(errorCode = ErrorCode.MISSING_API_KEY),
                )
            }
        }
    }

    fun selectSettings() {
        logger.i(TAG) { "Navigating to settings" }
        navigator.navigate(FeatureRoute.Settings)
    }

    fun selectTab(index: Int) {
        logger.i(TAG) { "Tab selected: $index" }
        _state.value = _state.value.copy(selectedIndex = index)
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
)

sealed class Tab(val name: StringResource) {
    data object Avatar : Tab(Res.string.tab_avatars)
    data object Messages : Tab(Res.string.tab_messages)
    data object Llms : Tab(Res.string.tab_llms)
    data object Voices : Tab(Res.string.tab_voices)
}
