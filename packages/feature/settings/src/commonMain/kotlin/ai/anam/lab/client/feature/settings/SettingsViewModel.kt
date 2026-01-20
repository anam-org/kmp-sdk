package ai.anam.lab.client.feature.settings

import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.settings.Theme
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(SettingsViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class SettingsViewModel(
    private val preferences: AnamPreferences,
    private val logger: Logger,
    private val navigator: Navigator,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.theme.flow.collect { theme ->
                logger.i(TAG) { "Theme updated: $theme" }
                _state.value = _state.value.copy(theme = theme)
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

data class SettingsViewState(val theme: Theme = Theme.SYSTEM)
