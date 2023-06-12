package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.ktx.now
import kotlinx.datetime.LocalDateTime

object Log {
    fun e(tag: String, message: String, e: Throwable? = null) {
        if (e == null) {
            synchronized(logLines) { log(LogLine('E', tag, message)) }
            android.util.Log.e(tag, message)
        } else {
            synchronized(logLines) { log(LogLine('E', tag, "$message\n${e.stackTraceToString()}")) }
            android.util.Log.e(tag, message, e)
        }
    }

    fun w(tag: String, message: String, e: Throwable? = null) {
        if (e == null) {
            synchronized(logLines) { log(LogLine('W', tag, message)) }
            android.util.Log.e(tag, message)
        } else {
                synchronized(logLines) { log(LogLine('W', tag, "$message\n${e.stackTraceToString()}")) }
            android.util.Log.e(tag, message, e)
        }
    }

    fun i(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('I', tag, message)) }
        android.util.Log.i(tag, message)
    }

    fun d(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('D', tag, message)) }
        android.util.Log.d(tag, message)
    }

    fun v(tag: String, message: String) {
        synchronized(logLines) { log(LogLine('V', tag, message)) }
        android.util.Log.v(tag, message)
    }

    private fun log(line: LogLine) {
        synchronized(logLines) {
            if (logLines.size > 12000) // clear oldest entries if list gets too long
                logLines.subList(0, 2000).clear()
            logLines.add(line)
        }
//        println(logLine) // would be nice for testing only, but how to do?
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
