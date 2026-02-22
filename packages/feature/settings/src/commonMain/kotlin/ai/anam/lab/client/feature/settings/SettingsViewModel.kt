package ai.anam.lab.client.feature.settings

import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.settings.AnamPreferences
import ai.anam.lab.client.core.settings.Theme
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.GetApiKeyInteractor
import ai.anam.lab.client.domain.data.UpdateApiKeyInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class SettingsViewModel(
    private val preferences: AnamPreferences,
    private val logger: Logger,
    private val navigator: Navigator,
    private val updateApiKeyInteractor: UpdateApiKeyInteractor,
    private val getApiKeyInteractor: GetApiKeyInteractor,
) : BaseViewModel<SettingsViewState>(SettingsViewState()) {

    init {
        setState { copy(displayApiKey = formatDisplayKey(getApiKeyInteractor())) }

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

    fun showApiKeyDialog() {
        setState { copy(showApiKeyDialog = true, apiKey = getApiKeyInteractor() ?: "") }
    }

    fun dismissApiKeyDialog() {
        setState { copy(showApiKeyDialog = false) }
    }

    fun saveApiKey(key: String) {
        logger.i(TAG) { "Saving API key" }
        viewModelScope.launch {
            updateApiKeyInteractor(key)
            setState {
                copy(
                    displayApiKey = formatDisplayKey(getApiKeyInteractor()),
                    showApiKeyDialog = false,
                )
            }
        }
    }

    private fun formatDisplayKey(key: String?): String? {
        if (key.isNullOrEmpty()) return null
        return if (key.length > DISPLAY_KEY_LENGTH) {
            key.take(DISPLAY_KEY_LENGTH) + "..."
        } else {
            key
        }
    }

    private companion object {
        const val TAG = "SettingsViewModel"
        const val DISPLAY_KEY_LENGTH = 8
    }
}

data class SettingsViewState(
    val theme: Theme = Theme.SYSTEM,
    val displayApiKey: String? = null,
    val showApiKeyDialog: Boolean = false,
    val apiKey: String = "",
) : ViewState
