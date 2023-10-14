package de.westnordost.streetcomplete.screens.about

import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsDao
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import de.westnordost.streetcomplete.util.logs.Log
import kotlinx.datetime.LocalDateTime

class LogsController(private val logsDao: LogsDao) {
    fun getLogs(filters: LogsFilters): List<LogMessage> {
        return logsDao.getAll(
            levels = filters.levels,
            messageContains = filters.messageContains,
            newerThan = filters.timestampNewerThan?.toEpochMilli(),
            olderThan = filters.timestampOlderThan?.toEpochMilli()
        )
    }

    fun deleteOlderThan(timestamp: Long) {
        val deletedCount = logsDao.deleteOlderThan(timestamp)
        if (deletedCount > 0) {
            Log.v(TAG, "Deleted $deletedCount old log messages")
        }
    }

    companion object {
        private const val TAG = "LogsController"
    }
}

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
}
