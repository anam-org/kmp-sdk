package ai.anam.lab.client.core.navigation

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface NavigationEvent {
    data class Navigate(val route: FeatureRoute) : NavigationEvent
    data object Pop : NavigationEvent
}

interface Navigator {
    val events: Flow<NavigationEvent>
    fun navigate(route: FeatureRoute)
    fun pop()
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
class NavigatorImpl : Navigator {
    private val _events = Channel<NavigationEvent>(Channel.BUFFERED)
    override val events = _events.receiveAsFlow()

    override fun navigate(route: FeatureRoute) {
        _events.trySend(NavigationEvent.Navigate(route))
    }

    override fun pop() {
        _events.trySend(NavigationEvent.Pop)
    }
}
