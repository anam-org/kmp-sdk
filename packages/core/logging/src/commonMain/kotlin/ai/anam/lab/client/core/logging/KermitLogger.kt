package ai.anam.lab.client.core.logging

import co.touchlab.kermit.Logger as Kermit
import co.touchlab.kermit.Severity as KermitSeverity

/**
 * Implementation of [Logger] that internally uses [Kermit].
 */
class KermitLogger(private val kermit: Kermit = Kermit) : Logger {
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
