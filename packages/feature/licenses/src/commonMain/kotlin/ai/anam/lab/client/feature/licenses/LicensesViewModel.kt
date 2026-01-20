package ai.anam.lab.client.feature.licenses

import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import ai.anam.lab.client.core.licenses.LicenseItem
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.domain.data.FetchLicensesInteractor
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Inject
@ViewModelKey(LicensesViewModel::class)
@ContributesIntoMap(ViewModelScope::class)
class LicensesViewModel(
    private val fetchLicenses: FetchLicensesInteractor,
    private val logger: Logger,
    private val navigator: Navigator,
) : ViewModel() {

    private val _state = MutableStateFlow(LicensesViewState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            logger.i(TAG) { "Fetching licenses..." }
            try {
                val licenses = fetchLicenses()
                    .groupBy { it.groupId }
                    .map { (groupId, artifacts) ->
                        LicenseGroup(
                            id = groupId,
                            artifacts = artifacts.sortedBy { it.artifactId },
                        )
                    }
                    .sortedBy { it.id }

                _state.value = _state.value.copy(licenses = licenses)
            } catch (e: Exception) {
                logger.e(TAG, e) { "Failed to fetch licenses" }
            }
        }
    }

    fun navigateBack() {
        logger.i(TAG) { "Navigating back" }
        navigator.pop()
    }

    private companion object {
        const val TAG = "LicensesViewModel"
    }
}

data class LicensesViewState(val licenses: List<LicenseGroup> = emptyList())

data class LicenseGroup(val id: String, val artifacts: List<LicenseItem>)
