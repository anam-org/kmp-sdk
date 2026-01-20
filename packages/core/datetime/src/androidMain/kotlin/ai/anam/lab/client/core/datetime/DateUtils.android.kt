package ai.anam.lab.client.core.datetime

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

actual fun Instant.toFormattedDateString(): String {
    val date = Date(this.toEpochMilliseconds())
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
    return formatter.format(date)
}
