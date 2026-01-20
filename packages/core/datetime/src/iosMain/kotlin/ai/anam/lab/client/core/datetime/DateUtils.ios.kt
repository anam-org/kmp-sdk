package ai.anam.lab.client.core.datetime

import kotlin.time.Instant
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.dateWithTimeIntervalSince1970

actual fun Instant.toFormattedDateString(): String {
    val date = NSDate.dateWithTimeIntervalSince1970(this.toEpochMilliseconds() / 1000.0)
    val formatter = NSDateFormatter()
    formatter.locale = NSLocale.currentLocale

    formatter.dateFormat = "d"
    val dayString = formatter.stringFromDate(date)
    val day = dayString.toIntOrNull() ?: 0

    formatter.dateFormat = "MMMM yyyy"
    val monthYear = formatter.stringFromDate(date)
    return "$day $monthYear"
}
