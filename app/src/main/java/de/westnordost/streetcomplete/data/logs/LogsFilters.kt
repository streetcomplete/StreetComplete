package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LogsFilters(
    var levels: MutableSet<LogLevel> = LogLevel.values().toMutableSet(),
    var messageContains: String? = null,
    var timestampNewerThan: LocalDateTime? = null,
    var timestampOlderThan: LocalDateTime? = null
) {
    fun copy(): LogsFilters = LogsFilters(
        levels.toMutableSet(),
        messageContains,
        timestampNewerThan,
        timestampOlderThan
    )

    fun matches(message: LogMessage): Boolean {
        if (!levels.contains(message.level)) {
            return false
        }

        val messageContains = messageContains
        if (messageContains != null && !message.message.contains(messageContains, ignoreCase = true)) {
            return false
        }

        val timestampNewerThan = timestampNewerThan
        if (timestampNewerThan != null && message.timestamp <= timestampNewerThan.toEpochMilli()) {
            return false
        }

        val timestampOlderThan = timestampOlderThan
        if (timestampOlderThan != null && message.timestamp >= timestampOlderThan.toEpochMilli()) {
            return false
        }

        return true
    }
}
