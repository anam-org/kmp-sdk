package ai.anam.lab.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * This function allows you to map a flow, but have access to the current item (if one is available) during the
 * transformation.
 */
internal fun <T> Flow<T>.mapWithCurrent(transform: suspend (previous: T?, next: T) -> T): Flow<T> = flow {
    var previous: T? = null
    collect { item ->
        val transformed = transform(previous, item)
        emit(transformed)
        previous = transformed
    }
}
