package de.westnordost.streetcomplete.util.logs

interface Logger {
    /** Send VERBOSE log message */
    fun v(tag: String, message: String)

    /** Send DEBUG log message */
    fun d(tag: String, message: String)

    /** Send INFO log message */
    fun i(tag: String, message: String)

    /** Send WARNING log message */
    fun w(tag: String, message: String)

    /** Send WARNING log message and exception */
    fun w(tag: String, message: String, e: Throwable)

    /** Send ERROR log message */
    fun e(tag: String, message: String)

    /** Send ERROR log message and exception */
    fun e(tag: String, message: String, e: Throwable)
}
