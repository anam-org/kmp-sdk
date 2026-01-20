package ai.anam.lab.client.core.viewmodel

import ai.anam.lab.client.core.di.ViewModelGraph
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras

/**
 * Factory interface to create a new [ViewModelGraph].
 */
fun interface ViewModelGraphProvider : ViewModelProvider.Factory {
    fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph
}
