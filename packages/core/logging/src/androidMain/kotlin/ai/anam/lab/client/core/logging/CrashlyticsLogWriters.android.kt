package ai.anam.lab.client.core.logging

import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter

actual fun initCrashReporting() = Unit

@OptIn(ExperimentalKermitApi::class)
actual fun crashlyticsLogWriters(): List<LogWriter> = listOf(CrashlyticsLogWriter())
