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

    /**
     * Returns the current value without suspending. Reads directly from the backing store, which
     * is synchronous on all platforms. Prefer the suspend [get] for general use; this exists for
     * the narrow case where a value must be available before any coroutine can run (e.g. during
     * app initialisation).
     */
    fun getBlocking(): T
}

/**
 * Toggles the value of the preference.
 */
suspend fun Preference<Boolean>.toggle() = set(!get())
