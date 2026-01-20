package ai.anam.lab.client.core.logging

/**
 * Simple logging interface.
 */
interface Logger {
    fun v(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })

    fun d(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })

    fun i(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })

    fun w(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })

    fun e(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })

    fun assert(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
}
