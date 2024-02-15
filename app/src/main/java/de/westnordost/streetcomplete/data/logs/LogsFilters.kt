package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class LogsFilters(
    val levels: Set<LogLevel> = LogLevel.entries.toSet(),
    val messageContains: String? = null,
    val timestampNewerThan: LocalDateTime? = null,
    val timestampOlderThan: LocalDateTime? = null
) {
    fun matches(log: LogMessage): Boolean =
        levels.contains(log.level) &&
        (messageContains == null ||
            log.message.contains(messageContains, ignoreCase = true) ||
            log.tag.contains(messageContains, ignoreCase = true)
        ) &&
        (timestampNewerThan == null || log.timestamp > timestampNewerThan.toEpochMilli()) &&
        (timestampOlderThan == null || log.timestamp < timestampOlderThan.toEpochMilli())
}
