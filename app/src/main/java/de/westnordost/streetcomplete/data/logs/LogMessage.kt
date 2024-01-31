package de.westnordost.streetcomplete.data.logs

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class LogMessage(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val error: String?,
    val timestamp: Long
) {
    override fun toString(): String {
        var string = "[$tag] $message"

        if (error != null) {
            string += " $error"
        }

        return string
    }
}

fun Iterable<LogMessage>.format(tz: TimeZone = TimeZone.currentSystemDefault()): String {
    return joinToString("\n") {
        val timestamp = Instant.fromEpochMilliseconds(it.timestamp)
            .toLocalDateTime(tz)
            .toString()

        "$timestamp: $it"
    }
}
