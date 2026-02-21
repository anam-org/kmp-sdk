package ai.anam.lab.utils

/**
 * Finds an existing item by [id] and merges it with [item] using [merge], or appends [item] if no match is found.
 */
internal fun <T> List<T>.updateOrAppend(item: T, id: (T) -> String, merge: (existing: T, incoming: T) -> T): List<T> {
    val index = indexOfFirst { id(it) == id(item) }
    return if (index == -1) {
        this + item
    } else {
        toMutableList().apply {
            set(index, merge(get(index), item))
        }
    }
}
