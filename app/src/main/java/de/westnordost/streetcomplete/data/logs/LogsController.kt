package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.util.Listeners

class LogsController(private val logsDao: LogsDao) {

    /** Interface to be notified of new log messages */
    interface Listener {
        fun onAdded(message: LogMessage)
    }

    private val listeners = Listeners<Listener>()

    fun getLogs(
        levels: Set<LogLevel> = LogLevel.entries.toSet(),
        messageContains: String? = null,
        newerThan: Long? = null,
        olderThan: Long? = null,
    ): List<LogMessage> =
        logsDao.getAll(levels, messageContains, newerThan, olderThan)

    fun deleteOlderThan(timestamp: Long) {
        logsDao.deleteOlderThan(timestamp)
    }

    fun clear() {
        logsDao.clear()
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
}
