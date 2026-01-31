package ai.anam.lab.client.feature.llms

import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides

@ContributesTo(ViewModelScope::class)
interface LlmsFeatureViewSubgraph {
    @Provides
    @IntoMap
    @ViewModelKey(LlmsViewModel::class)
    fun providesLlmsViewModel(provider: Provider<LlmsViewModel>): ViewModel = provider()
}
