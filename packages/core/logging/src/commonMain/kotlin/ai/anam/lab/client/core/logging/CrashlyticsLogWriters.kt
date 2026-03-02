package ai.anam.lab.client.core.logging

import co.touchlab.kermit.LogWriter

/**
 * Performs one-time platform-specific crash reporting initialization.
 *
 * On iOS, this enables CrashKiOS and installs the unhandled exception hook so that Kotlin/Native crashes are properly
 * reported to Firebase Crashlytics. On Android and wasmJs, this is a no-op (Firebase auto-initializes on Android;
 * wasmJs has no Crashlytics). Safe to call multiple times — subsequent calls are ignored.
 */
expect fun initCrashReporting()

/**
 * Returns platform-specific [LogWriter]s that forward logs to Firebase Crashlytics.
 *
 * On Android and iOS, this returns a [CrashlyticsLogWriter] that sends breadcrumbs and non-fatal exceptions to
 * Crashlytics. On wasmJs, this returns an empty list (no-op).
 */
expect fun crashlyticsLogWriters(): List<LogWriter>
