package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.data.CursorPosition
import de.westnordost.streetcomplete.data.Database
import de.westnordost.streetcomplete.data.logs.LogsTable.Columns.ERROR
import de.westnordost.streetcomplete.data.logs.LogsTable.Columns.LEVEL
import de.westnordost.streetcomplete.data.logs.LogsTable.Columns.MESSAGE
import de.westnordost.streetcomplete.data.logs.LogsTable.Columns.TAG
import de.westnordost.streetcomplete.data.logs.LogsTable.Columns.TIMESTAMP
import de.westnordost.streetcomplete.data.logs.LogsTable.NAME

/** Stores the app logs */
class LogsDao(private val db: Database) {
    fun getAll(
        levels: Set<LogLevel> = LogLevel.values().toSet(),
        messageContains: String? = null,
        newerThan: Long? = null,
        olderThan: Long? = null,
    ): List<LogMessage> {
        val levelsString = levels.joinToString(",") { "'${it.name}'" }
        var where = "$LEVEL IN ($levelsString)"
        var args = arrayOf<Any>()

        if (messageContains != null) {
            where += " AND $MESSAGE LIKE ?"
            args += "%$messageContains%"
        }

        if (newerThan != null) {
            where += " AND $TIMESTAMP > ?"
            args += newerThan
        }

        if (olderThan != null) {
            where += " AND $TIMESTAMP < ?"
            args += olderThan
        }

        return db.query(
            NAME,
            where = where,
            args = args,
            orderBy = "$TIMESTAMP ASC"
        ) { it.toLogMessage() }
    }

    fun add(message: LogMessage) {
        db.insert(NAME, message.toPairs())
    }

    fun deleteOlderThan(time: Long): Int = db.delete(NAME, where = "$TIMESTAMP < $time")
}

private fun LogMessage.toPairs(): List<Pair<String, Any?>> = listOfNotNull(
    LEVEL to level.name,
    TAG to tag,
    MESSAGE to message,
    ERROR to error,
    TIMESTAMP to timestamp
)

private fun CursorPosition.toLogMessage() = LogMessage(
    LogLevel.valueOf(getString(LEVEL)),
    getString(TAG),
    getString(MESSAGE),
    getStringOrNull(ERROR),
    getLong(TIMESTAMP)
)
