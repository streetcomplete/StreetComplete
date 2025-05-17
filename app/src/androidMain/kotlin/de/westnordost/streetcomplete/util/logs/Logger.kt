package de.westnordost.streetcomplete.util.logs

interface Logger {
    /** Send VERBOSE log [message] */
    fun v(tag: String, message: String)

    /** Send DEBUG log [message] */
    fun d(tag: String, message: String)

    /** Send INFO log [message] */
    fun i(tag: String, message: String)

    /** Send WARNING log [message] with optional [exception] */
    fun w(tag: String, message: String, exception: Throwable? = null)

    /** Send ERROR log [message] with optional [exception] */
    fun e(tag: String, message: String, exception: Throwable? = null)
}
