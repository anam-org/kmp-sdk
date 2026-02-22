package ai.anam.lab.client.core.test

import ai.anam.lab.client.core.navigation.FeatureRoute
import ai.anam.lab.client.core.navigation.NavigationEvent
import ai.anam.lab.client.core.navigation.Navigator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeNavigator : Navigator {
    override val events: Flow<NavigationEvent> = emptyFlow()

    var lastRoute: FeatureRoute? = null
        private set

    override fun navigate(route: FeatureRoute) {
        lastRoute = route
    }

    override fun pop() {}
}
