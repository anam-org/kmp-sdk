package ai.anam.lab.client.core.settings

import ai.anam.lab.client.core.di.Dispatcher
import ai.anam.lab.client.core.di.DispatcherType
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.set
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext

/**
 * The user preferences for the application.
 */
interface AnamPreferences {
    val theme: Preference<Theme>
}

@OptIn(ExperimentalSettingsApi::class)
@Inject
@ContributesBinding(AppScope::class)
class AnamPreferencesImpl(
    settings: Lazy<ObservableSettings>,
    @Dispatcher(DispatcherType.IO) private val ioDispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
) : AnamPreferences {
    private val settings: ObservableSettings by settings
    private val flowSettings by lazy { settings.value.toFlowSettings(ioDispatcher) }

    override val theme: Preference<Theme> by lazy {
        MappingPreference(KEY_THEME, Theme.SYSTEM, ::getThemeForStorageValue, ::themeToStorageValue)
    }

    private inner class MappingPreference<V>(
        private val key: String,
        override val defaultValue: V,
        private val toValue: (String) -> V,
        private val fromValue: (V) -> String,
    ) : Preference<V> {
        override suspend fun set(value: V) = withContext(ioDispatcher) {
            settings[key] = fromValue(value)
        }

        override suspend fun get(): V = withContext(ioDispatcher) {
            settings.getStringOrNull(key)?.let(toValue) ?: defaultValue
        }

        override val flow: Flow<V> by lazy {
            flowSettings.getStringOrNullFlow(key)
                .map { it?.let(toValue) ?: defaultValue }
                .shareIn(
                    scope = coroutineScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIBED_TIMEOUT),
                )
        }
    }

    private fun getThemeForStorageValue(value: String) = when (value) {
        THEME_LIGHT_VALUE -> Theme.LIGHT
        THEME_DARK_VALUE -> Theme.DARK
        else -> Theme.SYSTEM
    }

    private fun themeToStorageValue(theme: Theme): String = when (theme) {
        Theme.LIGHT -> THEME_LIGHT_VALUE
        Theme.DARK -> THEME_DARK_VALUE
        Theme.SYSTEM -> THEME_SYSTEM_VALUE
    }

    internal companion object {
        const val KEY_THEME = "pref_theme"
        const val THEME_LIGHT_VALUE = "light"
        const val THEME_DARK_VALUE = "dark"
        const val THEME_SYSTEM_VALUE = "system"

        private val SUBSCRIBED_TIMEOUT = 20.seconds
    }
}

/**
 * The user's preference for the color theme to be applied.
 */
enum class Theme {
    LIGHT,
    DARK,
    SYSTEM,
}
