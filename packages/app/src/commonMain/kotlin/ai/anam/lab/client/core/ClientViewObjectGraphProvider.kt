package ai.anam.lab.client.core

import ai.anam.lab.client.core.di.ViewModelGraph
import ai.anam.lab.client.core.viewmodel.ViewModelGraphProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.CreationExtras
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass
import kotlin.reflect.cast

@Inject
@ContributesBinding(AppScope::class)
class ClientViewObjectGraphProvider(private val appGraph: ClientAppObjectGraph) : ViewModelGraphProvider {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        val viewModelGraph = buildViewModelGraph(extras)
        println(viewModelGraph.viewModelProviders)
        val viewModelProvider = requireNotNull(viewModelGraph.viewModelProviders[modelClass]) {
            "Unknown model class $modelClass"
        }
        return modelClass.cast(viewModelProvider())
    }

    override fun buildViewModelGraph(extras: CreationExtras): ViewModelGraph {
        return (appGraph as HasClientViewObjectGraph).clientViewObjectGraphFactory.create(extras)
    }
}

interface HasClientViewObjectGraph {
    val clientViewObjectGraphFactory: ClientViewObjectGraph.Factory
}
