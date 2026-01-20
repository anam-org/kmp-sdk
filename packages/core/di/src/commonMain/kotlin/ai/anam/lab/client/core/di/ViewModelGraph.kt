package ai.anam.lab.client.core.di

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provider
import kotlin.reflect.KClass

interface ViewModelGraph {
    /**
     * Contains all providers (aka factories) for specific [ViewModel]s.
     */
    @Multibinds
    val viewModelProviders: Map<KClass<out ViewModel>, Provider<ViewModel>>
}
