package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.util.Listeners
import de.westnordost.streetcomplete.util.logs.Log

class LogsController(private val logsDao: LogsDao) {

    /** Interface to be notified of new log messages */
    interface Listener {
        fun onAdded(message: LogMessage)
    }

    private val listeners = Listeners<Listener>()

    fun getLogs(
        levels: Set<LogLevel> = LogLevel.values().toSet(),
        messageContains: String? = null,
        newerThan: Long? = null,
        olderThan: Long? = null,
    ): List<LogMessage> {
        return logsDao.getAll(
            levels = levels,
            messageContains = messageContains,
            newerThan = newerThan,
            olderThan = olderThan,
        )
    }

    fun deleteOlderThan(timestamp: Long) {
        val deletedCount = logsDao.deleteOlderThan(timestamp)
        if (deletedCount > 0) {
            Log.v(TAG, "Deleted $deletedCount old log messages")
        }
    }

    fun add(message: LogMessage) {
        logsDao.add(message)
        onAdded(message)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun onAdded(message: LogMessage) {
        listeners.forEach { it.onAdded(message) }
    }

    companion object {
        private const val TAG = "LogsController"
    }
}
