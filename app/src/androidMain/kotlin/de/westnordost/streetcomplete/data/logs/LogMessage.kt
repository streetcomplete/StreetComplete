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

fun Iterable<LogMessage>.format(tz: TimeZone = TimeZone.currentSystemDefault()): String =
    joinToString("\n") { logMessage ->
        val timestamp = Instant.fromEpochMilliseconds(logMessage.timestamp)
            .toLocalDateTime(tz)
            .toString()
        val logLevel = logMessage.level.toChar()
        "$timestamp: $logLevel $logMessage"
    }
