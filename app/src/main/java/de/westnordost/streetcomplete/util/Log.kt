package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.ktx.now
import kotlinx.datetime.LocalDateTime

object Log {
    fun e(tag: String, message: String, e: Throwable? = null) {
        if (e == null) {
            synchronized(logLines) { log('E', tag, message) }
            android.util.Log.e(tag, message)
        } else {
            synchronized(logLines) { log('E', tag, "$message\n${e.stackTraceToString()}") }
            android.util.Log.e(tag, message, e)
        }
    }

    fun w(tag: String, message: String, e: Throwable? = null) {
        if (e == null) {
            synchronized(logLines) { log('W', tag, message) }
            android.util.Log.e(tag, message)
        } else {
                synchronized(logLines) { log('W', tag, "$message\n${e.stackTraceToString()}") }
            android.util.Log.e(tag, message, e)
        }
    }

    fun i(tag: String, message: String) {
        synchronized(logLines) { log('I', tag, message) }
        android.util.Log.i(tag, message)
    }

    fun d(tag: String, message: String) {
        synchronized(logLines) { log('D', tag, message) }
        android.util.Log.d(tag, message)
    }

    fun v(tag: String, message: String) {
        synchronized(logLines) { log('V', tag, message) }
        android.util.Log.v(tag, message)
    }

    private fun log(level: Char, tag: String, message: String) {
        val dateTime = LocalDateTime.now().toString().replace('T', ' ')
        val logLine = "$dateTime $level $tag: $message"
        synchronized(logLines) {
            if (logLines.size > 12000) // clear oldest entries if list gets too long
                logLines.subList(0, 2000).clear()
            logLines.add(logLine)
        }
//        println(logLine) // would be nice for testing only, but how to do?
    }

    val logLines: MutableList<String> = ArrayList(2000)
}
