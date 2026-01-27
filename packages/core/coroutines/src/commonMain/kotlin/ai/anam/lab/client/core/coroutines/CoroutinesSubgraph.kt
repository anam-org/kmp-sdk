package ai.anam.lab.client.core.coroutines

import ai.anam.lab.client.core.di.Dispatcher
import ai.anam.lab.client.core.di.DispatcherType
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * This subgraph provides access to the various [CoroutineDispatcher]'s. These can be access via the [@Dispatcher]
 * annotation.
 */
@ContributesTo(AppScope::class)
interface CoroutinesSubgraph {

    @Dispatcher(DispatcherType.Main)
    @Provides
    fun providesMainDispatcher(): CoroutineDispatcher = DefaultDispatchers.Main

    @Dispatcher(DispatcherType.IO)
    @Provides
    fun providesIoDispatcher(): CoroutineDispatcher = DefaultDispatchers.IO

    @Dispatcher(DispatcherType.Default)
    @Provides
    fun providesDefaultDispatcher(): CoroutineDispatcher = DefaultDispatchers.Default

    /**
     * The main application scope. This should ideally not be used frequently, but when required, can be injected.
     */
    @Provides
    @SingleIn(AppScope::class)
    fun provideApplicationCoroutineScope(
        @Dispatcher(DispatcherType.Main) mainDispatcher: CoroutineDispatcher,
    ): CoroutineScope = CoroutineScope(mainDispatcher + SupervisorJob())
}
