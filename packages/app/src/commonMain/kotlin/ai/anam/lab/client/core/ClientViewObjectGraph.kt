package ai.anam.lab.client.core

import ai.anam.lab.client.core.di.ViewModelGraph
import ai.anam.lab.client.core.di.ViewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import dev.zacsweers.metro.Provides

@GraphExtension(ViewModelScope::class)
interface ClientViewObjectGraph : ViewModelGraph {

    /**
     * Factory class that can build a new [ClientViewObjectGraph], which will inherit bindings from the associated
     * [AppScope].
     */
    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun create(@Provides extras: CreationExtras): ClientViewObjectGraph
    }
}
