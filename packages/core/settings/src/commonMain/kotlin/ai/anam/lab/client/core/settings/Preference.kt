package ai.anam.lab.client.core.settings

import kotlinx.coroutines.flow.Flow

/**
 * Represents a preference that can be set and retrieved.
 */
interface Preference<T> {
    val defaultValue: T

    /**
     * The flow that emits the current value of the preference.
     */
    val flow: Flow<T>

    suspend fun set(value: T)
    suspend fun get(): T
}

/**
 * Toggles the value of the preference.
 */
suspend fun Preference<Boolean>.toggle() = set(!get())
