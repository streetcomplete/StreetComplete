package de.westnordost.streetcomplete.data.logs

interface LogsSource {
    /** Interface to be notified of new log messages */
    interface Listener {
        fun onAdded(message: LogMessage)
    }

    fun getLogs(
        levels: Set<LogLevel> = LogLevel.entries.toSet(),
        messageContains: String? = null,
        newerThan: Long? = null,
        olderThan: Long? = null,
    ): List<LogMessage>

    fun addListener(listener: Listener)
    fun removeListener(listener: Listener)
}
