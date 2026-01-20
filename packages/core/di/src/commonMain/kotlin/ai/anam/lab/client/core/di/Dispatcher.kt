package ai.anam.lab.client.core.di

import dev.zacsweers.metro.Qualifier

/**
 * Qualifier for specifying the type of Dispatcher required during DI.
 */
@Qualifier
annotation class Dispatcher(val type: DispatcherType)

/**
 * The support dispatcher types.
 */
enum class DispatcherType {
    Main,
    IO,
    Default,
}
