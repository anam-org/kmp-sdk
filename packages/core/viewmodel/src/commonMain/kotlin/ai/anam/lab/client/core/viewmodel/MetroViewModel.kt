package ai.anam.lab.client.core.viewmodel

import ai.anam.lab.client.core.di.ViewModelGraph
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Fetches [ViewModel] instances from the [ViewModelGraph]
 */
@Composable
inline fun <reified VM : ViewModel> metroViewModel(
    owner: ViewModelStoreOwner = requireViewModelStoreOwner(),
    key: String? = null,
): VM = viewModel(viewModelStoreOwner = owner, key = key, factory = LocalViewModelGraphProvider.current)

@Composable
fun requireViewModelStoreOwner() = checkNotNull(LocalViewModelStoreOwner.current) {
    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
}
