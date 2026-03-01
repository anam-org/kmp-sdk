package ai.anam.lab.client.core.logging

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
private val initialized = AtomicBoolean(false)

/**
 * Initializes CrashKiOS for Kotlin/Native crash reporting on iOS.
 *
 * Firebase Crashlytics (added via CocoaPods) captures Swift/ObjC crashes automatically. CrashKiOS adds support for
 * Kotlin/Native exceptions — without it, Kotlin stack frames in crash reports would be unsymbolicated.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun initCrashReporting() {
    if (!initialized.compareAndSet(expectedValue = false, newValue = true)) return
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}

@OptIn(ExperimentalKermitApi::class)
actual fun crashlyticsLogWriters(): List<LogWriter> = listOf(CrashlyticsLogWriter())
