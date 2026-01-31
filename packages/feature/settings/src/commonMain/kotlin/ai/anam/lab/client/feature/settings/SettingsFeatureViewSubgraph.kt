package ai.anam.lab.client.feature.settings

import ai.anam.lab.client.core.di.ViewModelKey
import ai.anam.lab.client.core.di.ViewModelScope
import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.Provides

@ContributesTo(ViewModelScope::class)
interface SettingsFeatureViewSubgraph {
    @Provides
    @IntoMap
    @ViewModelKey(SettingsViewModel::class)
    fun providesSettingsViewModel(provider: Provider<SettingsViewModel>): ViewModel = provider()
}
