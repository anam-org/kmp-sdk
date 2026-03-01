package ai.anam.lab.client.core.logging

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger as Kermit
import co.touchlab.kermit.Severity
import co.touchlab.kermit.StaticConfig
import kotlin.test.Test

/**
 * Tests for [KermitLogger] covering severity mapping, tag forwarding, and min-severity filtering.
 *
 * Note: [crashlyticsLogWriters] and [initCrashReporting] are not covered here because `kermit-crashlytics` links
 * against Firebase native symbols that are unavailable in the test binary. Those paths are verified manually via the
 * steps in `docs/firebase-crashlytics-setup.md`.
 */
class KermitLoggerTest {

    @Test
    fun `v logs at Verbose severity`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer, minSeverity = Severity.Verbose)

        logger.v("tag") { "message" }

        assertThat(writer.entries).containsExactly(LogEntry(Severity.Verbose, "tag", "message", null))
    }

    @Test
    fun `d logs at Debug severity`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer)

        logger.d("tag") { "message" }

        assertThat(writer.entries).containsExactly(LogEntry(Severity.Debug, "tag", "message", null))
    }

    @Test
    fun `i logs at Info severity`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer)

        logger.i("tag") { "message" }

        assertThat(writer.entries).containsExactly(LogEntry(Severity.Info, "tag", "message", null))
    }

    @Test
    fun `w logs at Warn severity`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer)

        logger.w("tag") { "message" }

        assertThat(writer.entries).containsExactly(LogEntry(Severity.Warn, "tag", "message", null))
    }

    @Test
    fun `e logs at Error severity with throwable`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer)
        val exception = RuntimeException("boom")

        logger.e("tag", exception) { "message" }

        assertThat(writer.entries).containsExactly(LogEntry(Severity.Error, "tag", "message", exception))
    }

    @Test
    fun `assert logs at Assert severity`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer)

        logger.assert("tag") { "message" }

        assertThat(writer.entries).containsExactly(LogEntry(Severity.Assert, "tag", "message", null))
    }

    @Test
    fun `messages below minSeverity are filtered`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer, minSeverity = Severity.Warn)

        logger.d("tag") { "debug" }
        logger.i("tag") { "info" }
        logger.w("tag") { "warn" }

        assertThat(writer.entries.map { it.severity }).containsExactly(Severity.Warn)
    }

    @Test
    fun `explicit tag is forwarded`() {
        val writer = RecordingLogWriter()
        val logger = createLogger(writer)

        logger.i("MyTag") { "message" }

        assertThat(writer.entries.first().tag).isEqualTo("MyTag")
    }

    @Test
    fun `null tag uses kermit default`() {
        val writer = RecordingLogWriter()
        val kermit = Kermit(config = StaticConfig(minSeverity = Severity.Debug, logWriterList = listOf(writer)))
        val logger = KermitLogger(kermit)

        logger.i(tag = null) { "message" }

        assertThat(writer.entries.first().tag).isEqualTo(kermit.tag)
    }

    private fun createLogger(writer: RecordingLogWriter, minSeverity: Severity = Severity.Debug): KermitLogger =
        KermitLogger(
            Kermit(config = StaticConfig(minSeverity = minSeverity, logWriterList = listOf(writer))),
        )

    private data class LogEntry(
        val severity: Severity,
        val tag: String,
        val message: String,
        val throwable: Throwable?,
    )

    private class RecordingLogWriter : LogWriter() {
        val entries = mutableListOf<LogEntry>()

        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            entries.add(LogEntry(severity, tag, message, throwable))
        }
    }
}
