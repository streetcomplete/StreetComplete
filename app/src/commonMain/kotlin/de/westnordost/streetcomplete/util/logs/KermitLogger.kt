package de.westnordost.streetcomplete.util.logs

class KermitLogger : Logger {
    override fun v(tag: String, message: String) {
        co.touchlab.kermit.Logger.v(message, tag = tag)
    }

    override fun d(tag: String, message: String) {
        co.touchlab.kermit.Logger.d(message, tag = tag)
    }

    override fun i(tag: String, message: String) {
        co.touchlab.kermit.Logger.i(message, tag = tag)
    }

    override fun w(tag: String, message: String, exception: Throwable?) {
        co.touchlab.kermit.Logger.w(message, throwable = exception, tag = tag)
    }

    override fun e(tag: String, message: String, exception: Throwable?) {
        co.touchlab.kermit.Logger.e(message, throwable = exception, tag = tag)
    }
}
