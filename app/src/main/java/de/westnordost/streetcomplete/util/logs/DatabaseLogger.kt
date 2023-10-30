package de.westnordost.streetcomplete.util.logs

import de.westnordost.streetcomplete.data.logs.LogLevel.*
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.screens.about.LogsController
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DatabaseLogger(private val logsController: LogsController) : Logger {
    private val coroutineScope = CoroutineScope(SupervisorJob() + CoroutineName("DatabaseLogger") + Dispatchers.IO)

    override fun v(tag: String, message: String) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = VERBOSE,
                tag = tag,
                message = message,
                error = null,
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }

    override fun d(tag: String, message: String) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = DEBUG,
                tag = tag,
                message = message,
                error = null,
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }

    override fun i(tag: String, message: String) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = INFO,
                tag = tag,
                message = message,
                error = null,
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }

    override fun w(tag: String, message: String) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = WARNING,
                tag = tag,
                message = message,
                error = null,
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }

    override fun w(tag: String, message: String, e: Throwable) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = WARNING,
                tag = tag,
                message = message,
                error = e.toString(),
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }

    override fun e(tag: String, message: String) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = ERROR,
                tag = tag,
                message = message,
                error = null,
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }

    override fun e(tag: String, message: String, e: Throwable) {
        coroutineScope.launch {
            logsController.add(LogMessage(
                level = ERROR,
                tag = tag,
                message = message,
                error = e.toString(),
                timestamp = nowAsEpochMilliseconds()
            ))
        }
    }
}
