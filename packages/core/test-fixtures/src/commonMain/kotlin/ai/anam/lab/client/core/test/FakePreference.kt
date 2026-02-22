package ai.anam.lab.client.core.test

import ai.anam.lab.client.core.settings.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreference<T>(override val defaultValue: T) : Preference<T> {
    private val state = MutableStateFlow(defaultValue)

    override val flow: Flow<T> = state

    override suspend fun set(value: T) {
        state.value = value
    }

    override suspend fun get(): T = state.value

    override fun getBlocking(): T = state.value
}
