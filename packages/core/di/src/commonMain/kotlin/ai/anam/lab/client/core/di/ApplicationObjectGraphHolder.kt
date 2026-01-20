package ai.anam.lab.client.core.di

/**
 * Base interface for the application's object graph.
 */
interface BaseApplicationObjectGraph

/**
 * An object that should be used to store the application's [BaseApplicationObjectGraph]. This makes it available to
 * other components that may need direct access.
 */
object ApplicationObjectGraphHolder {
    private var graph: BaseApplicationObjectGraph? = null

    fun set(value: BaseApplicationObjectGraph) {
        graph = value
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : BaseApplicationObjectGraph> get(): T {
        val current = graph as? T ?: error("ApplicationObjectGraph not yet set")
        return current
    }
}
