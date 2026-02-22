package ai.anam.lab.client.core.auth

import ai.anam.lab.client.core.settings.AnamPreferences
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.concurrent.Volatile
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manages the API authentication token. The token is resolved from two sources with the following
 * priority:
 *
 * 1. **Stored preference** — set at runtime via [setApiKey] and persisted across launches.
 * 2. **Build-time value** — baked into [BuildConfig.API_TOKEN] from `local.properties`.
 *
 * The stored preference is loaded synchronously in `init` via [Preference.getBlocking] so the
 * token is guaranteed to be available before any ViewModel observes [getApiToken].
 */
@Inject
@SingleIn(AppScope::class)
class AuthRepository(private val preferences: AnamPreferences) {
    @Volatile
    private var cachedToken: String? = BuildConfig.API_TOKEN.ifEmpty { null }

    private val _apiKeyChanged = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Emits [Unit] each time the API key is changed via [setApiKey]. Uses a buffered flow so that
     * slow subscribers (e.g. those triggering network calls) do not block delivery to fast
     * subscribers. Subscribers that attach after an emission will not receive it (replay = 0).
     */
    val apiKeyChanged: SharedFlow<Unit> = _apiKeyChanged.asSharedFlow()

    init {
        val stored = preferences.apiKey.getBlocking()
        if (stored.isNotEmpty()) {
            cachedToken = stored
        }
    }

    /**
     * Returns the current API token, or `null` if no token is configured.
     */
    fun getApiToken(): String? = cachedToken

    /**
     * Updates the API key to [key] (trimmed). An empty or blank key clears the token.
     *
     * @return `true` if the key changed, `false` if it was identical to the current value.
     */
    suspend fun setApiKey(key: String): Boolean {
        val trimmed = key.trim()
        val newToken = trimmed.ifEmpty { null }
        if (newToken == cachedToken) return false
        cachedToken = newToken
        // Persisting "" is safe: the init block checks isNotEmpty() before overriding cachedToken,
        // so an empty stored value is effectively treated as "no key".
        preferences.apiKey.set(trimmed)
        _apiKeyChanged.emit(Unit)
        return true
    }
}
