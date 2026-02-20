package ai.anam.lab.client.core.navigation

import dev.zacsweers.metro.MapKey

/**
 * This contains all known features that are supported for navigation.
 */
enum class FeatureRoute(val route: String) {
    Home("home"),
    Settings("settings"),
    Licenses("licenses"),
    Create("create"),
}

@MapKey
annotation class FeatureRouteKey(val value: FeatureRoute)
