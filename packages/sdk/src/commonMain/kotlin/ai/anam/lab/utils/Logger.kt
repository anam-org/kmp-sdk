package ai.anam.lab.utils

import co.touchlab.kermit.Logger as Kermit
import co.touchlab.kermit.Severity as KermitSeverity

/**
 * Generic interface for logging in the SDK.
 */
public interface Logger {
    public fun v(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
    public fun d(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
    public fun i(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
    public fun w(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
    public fun e(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
    public fun assert(tag: String? = null, throwable: Throwable? = null, message: () -> String = { "" })
}

/**
 * Default internal [Logger] that uses [Kermit] for multiplatform support.
 */
internal class KermitLogger(private val kermit: Kermit = Kermit) : Logger {
    override fun v(tag: String?, throwable: Throwable?, message: () -> String) {
        kermit.logBlock(KermitSeverity.Verbose, tag ?: kermit.tag, throwable, message)
    }

    override fun d(tag: String?, throwable: Throwable?, message: () -> String) {
        kermit.logBlock(KermitSeverity.Debug, tag ?: kermit.tag, throwable, message)
    }

    override fun i(tag: String?, throwable: Throwable?, message: () -> String) {
        kermit.logBlock(KermitSeverity.Info, tag ?: kermit.tag, throwable, message)
    }

    override fun w(tag: String?, throwable: Throwable?, message: () -> String) {
        kermit.logBlock(KermitSeverity.Warn, tag ?: kermit.tag, throwable, message)
    }

    override fun e(tag: String?, throwable: Throwable?, message: () -> String) {
        kermit.logBlock(KermitSeverity.Error, tag ?: kermit.tag, throwable, message)
    }

    override fun assert(tag: String?, throwable: Throwable?, message: () -> String) {
        kermit.logBlock(KermitSeverity.Assert, tag ?: kermit.tag, throwable, message)
    }
}
