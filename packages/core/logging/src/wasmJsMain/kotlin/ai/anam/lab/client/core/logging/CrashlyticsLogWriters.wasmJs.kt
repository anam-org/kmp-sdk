package ai.anam.lab.client.core.logging

import co.touchlab.kermit.LogWriter

actual fun initCrashReporting() = Unit

actual fun crashlyticsLogWriters(): List<LogWriter> = emptyList()
