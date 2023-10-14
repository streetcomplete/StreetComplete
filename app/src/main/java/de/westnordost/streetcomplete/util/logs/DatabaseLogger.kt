package de.westnordost.streetcomplete.util.logs

import de.westnordost.streetcomplete.data.logs.LogLevel.*
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsDao
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DatabaseLogger(private val logsDao: LogsDao) : Logger {
    private val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("DatabaseLogger"))

    override fun v(tag: String, message: String) {
        coroutineScope.launch {
            logsDao.add(LogMessage(VERBOSE, tag, message, null, nowAsEpochMilliseconds()))
        }
    }

    override fun d(tag: String, message: String) {
        coroutineScope.launch {
            logsDao.add(LogMessage(DEBUG, tag, message, null, nowAsEpochMilliseconds()))
        }
    }

    override fun i(tag: String, message: String) {
        coroutineScope.launch {
            logsDao.add(LogMessage(INFO, tag, message, null, nowAsEpochMilliseconds()))
        }
    }

    override fun w(tag: String, message: String) {
        coroutineScope.launch {
            logsDao.add(LogMessage(WARNING, tag, message, null, nowAsEpochMilliseconds()))
        }
    }

    override fun w(tag: String, message: String, e: Throwable) {
        coroutineScope.launch {
            logsDao.add(LogMessage(WARNING, tag, message, e.toString(), nowAsEpochMilliseconds()))
        }
    }

    override fun e(tag: String, message: String) {
        coroutineScope.launch {
            logsDao.add(LogMessage(ERROR, tag, message, null, nowAsEpochMilliseconds()))
        }
    }

    override fun e(tag: String, message: String, e: Throwable) {
        coroutineScope.launch {
            logsDao.add(LogMessage(ERROR, tag, message, e.toString(), nowAsEpochMilliseconds()))
        }
    }
}
