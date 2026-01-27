package ai.anam.lab.client.core.datetime

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.time.Instant

/** Kotlin/Wasm requires js() to be a single expression in a top-level function or property initializer. */
@OptIn(ExperimentalWasmJsInterop::class)
private val formatDateJs: (Double) -> String =
    js(
        "(function(ms) { return new Date(ms).toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' }); })",
    )

actual fun Instant.toFormattedDateString(): String = formatDateJs(this.toEpochMilliseconds().toDouble())
