package ai.anam.lab.client.core.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Base class for ViewModels that manage UI state.
 *
 * Provides a simplified API for state management with atomic updates.
 *
 * @param S The type of the UI state, must implement [ViewState].
 * @param initialState The initial state value.
 */
abstract class BaseViewModel<S : ViewState>(initialState: S) : ViewModel() {
    @PublishedApi
    internal val mutableState: MutableStateFlow<S> = MutableStateFlow(initialState)
    val state: StateFlow<S> = mutableState.asStateFlow()

    /**
     * Updates the state atomically using the provided reducer function.
     *
     * Example usage:
     * ```
     * setState { copy(isLoading = true) }
     * ```
     *
     * @param reducer A function that receives the current state and returns the new state.
     */
    protected inline fun setState(reducer: S.() -> S) {
        mutableState.update { it.reducer() }
    }
}
