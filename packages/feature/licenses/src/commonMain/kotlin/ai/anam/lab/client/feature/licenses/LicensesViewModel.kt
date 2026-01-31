package ai.anam.lab.client.feature.licenses

import ai.anam.lab.client.core.licenses.LicenseItem
import ai.anam.lab.client.core.logging.Logger
import ai.anam.lab.client.core.navigation.Navigator
import ai.anam.lab.client.core.viewmodel.BaseViewModel
import ai.anam.lab.client.core.viewmodel.ViewState
import ai.anam.lab.client.domain.data.FetchLicensesInteractor
import androidx.lifecycle.viewModelScope
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.launch

@Inject
class LicensesViewModel(
    private val fetchLicenses: FetchLicensesInteractor,
    private val logger: Logger,
    private val navigator: Navigator,
) : BaseViewModel<LicensesViewState>(LicensesViewState()) {

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

                setState { copy(licenses = licenses) }
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

data class LicensesViewState(val licenses: List<LicenseGroup> = emptyList()) : ViewState

data class LicenseGroup(val id: String, val artifacts: List<LicenseItem>)
