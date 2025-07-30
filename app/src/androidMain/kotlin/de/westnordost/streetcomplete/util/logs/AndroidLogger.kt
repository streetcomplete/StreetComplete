package de.westnordost.streetcomplete.util.logs

import android.util.Log

class AndroidLogger : Logger {
    override fun v(tag: String, message: String) {
        Log.v(tag, message)
    }

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String, exception: Throwable?) {
        if (exception != null) Log.w(tag, message, exception) else Log.w(tag, message)
    }

    override fun e(tag: String, message: String, exception: Throwable?) {
        if (exception != null) Log.e(tag, message, exception) else Log.e(tag, message)
    }
}
