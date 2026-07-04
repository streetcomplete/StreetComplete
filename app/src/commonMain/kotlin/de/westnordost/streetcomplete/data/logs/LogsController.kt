package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.util.Listeners

class LogsController(private val logsDao: LogsDao) : LogsSource {

    private val listeners = Listeners<LogsSource.Listener>()

    override fun getLogs(
        levels: Set<LogLevel>,
        messageContains: String?,
        newerThan: Long?,
        olderThan: Long?,
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

    override fun addListener(listener: LogsSource.Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: LogsSource.Listener) {
        listeners.remove(listener)
    }

    private fun onAdded(message: LogMessage) {
        listeners.forEach { it.onAdded(message) }
    }
}
