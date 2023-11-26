package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.logs.Logger
import kotlinx.datetime.LocalDateTime

object TempLogger : Logger {
    override fun e(tag: String, message: String, exception: Throwable?) {
        if (exception == null) {
            synchronized(logLines) { log(LogLine('E', tag, message)) }
        } else {
            synchronized(logLines) { log(LogLine('E', tag, "$message\n${exception.stackTraceToString()}")) }
        }
    }

    override fun w(tag: String, message: String, exception: Throwable?) {
        if (exception == null) {
            synchronized(logLines) { log(LogLine('W', tag, message)) }
        } else {
            synchronized(logLines) { log(LogLine('W', tag, "$message\n${exception.stackTraceToString()}")) }
        }
    }

    override fun i(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('I', tag, message)) }
    }

    override fun d(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('D', tag, message)) }
    }

    override fun v(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('V', tag, message)) }
    }

    private fun log(line: LogLine) {
        synchronized(logLines) {
            if (logLines.size > 12000) // clear oldest entries if list gets too long
                logLines.subList(0, 2000).clear()
            logLines.add(line)
        }
    }

    private val logLines: MutableList<LogLine> = ArrayList(2000)

    /** returns a copy of [logLines] */
    fun getLog() = synchronized(logLines) { logLines.toList() }
}

data class LogLine(val level: Char, val tag: String, val message: String,) {
    val time = LocalDateTime.now()
    override fun toString(): String = // should look like a normal android log line
        "${time.toString().replace('T', ' ')} $level $tag: $message"
}
