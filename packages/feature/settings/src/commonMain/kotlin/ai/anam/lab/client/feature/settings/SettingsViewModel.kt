package ai.anam.lab.client.feature.settings

import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.settings.Theme
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class SettingsViewModel(
    private val preferences: AnamPreferences,
    private val logger: Logger,
    private val navigator: Navigator,
) : BaseViewModel<SettingsViewState>(SettingsViewState()) {

    init {
        viewModelScope.launch {
            preferences.theme.flow.collect { theme ->
                logger.i(TAG) { "Theme updated: $theme" }
                setState { copy(theme = theme) }
            }
        }
    }

    fun navigateBack() {
        logger.i(TAG) { "Navigating back" }
        navigator.pop()
    }

    fun selectTheme(theme: Theme) {
        logger.i(TAG) { "Theme selected: $theme" }
        viewModelScope.launch {
            preferences.theme.set(theme)
        }
    }

    fun selectLicenses() {
        logger.i(TAG) { "Licenses selected" }
        viewModelScope.launch {
            navigator.navigate(FeatureRoute.Licenses)
        }
    }

    private companion object {
        const val TAG = "SettingsViewModel"
    }
}

data class SettingsViewState(val theme: Theme = Theme.SYSTEM) : ViewState
