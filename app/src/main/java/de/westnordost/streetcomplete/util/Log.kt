package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.util.ktx.now
import kotlinx.datetime.LocalDateTime

object Log {
    fun e(tag: String, message: String, e: Throwable? = null) {
        if (e == null) {
            log('E', tag, message)
            android.util.Log.e(tag, message)
        } else {
            log('E', tag, "$message\n${e.stackTraceToString()}")
            android.util.Log.e(tag, message, e)
        }
    }

    fun w(tag: String, message: String, e: Throwable? = null) {
        if (e == null) {
            log('W', tag, message)
            android.util.Log.e(tag, message)
        } else {
            log('W', tag, "$message\n${e.stackTraceToString()}")
            android.util.Log.e(tag, message, e)
        }
    }

    fun i(tag: String, message: String) {
        log('I', tag, message)
        android.util.Log.i(tag, message)
    }

    fun d(tag: String, message: String) {
        log('D', tag, message)
        android.util.Log.d(tag, message)
    }

    fun v(tag: String, message: String) {
        log('V', tag, message)
        android.util.Log.v(tag, message)
    }

    private fun log(level: Char, tag: String, message: String) {
        val dateTime = LocalDateTime.now().toString().replace('T', ' ')
        val logLine = "$dateTime $level $tag: $message"
        if (logLines.size > 12000) // clear oldest entries if list gets too long
            logLines.subList(0, 2000).clear()
        logLines.add(logLine)
//        println(logLine)
    }

    val logLines = mutableListOf<String>()
}
