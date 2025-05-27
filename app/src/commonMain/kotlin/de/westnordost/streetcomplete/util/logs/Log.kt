package de.westnordost.streetcomplete.util.logs

object Log : Logger {
    var instances: MutableList<Logger> = mutableListOf()

    override fun v(tag: String, message: String) {
        instances.forEach {
            it.v(tag, message)
        }
    }

    override fun d(tag: String, message: String) {
        instances.forEach {
            it.d(tag, message)
        }
    }

    override fun i(tag: String, message: String) {
        instances.forEach {
            it.i(tag, message)
        }
    }

    override fun w(tag: String, message: String, exception: Throwable?) {
        instances.forEach {
            it.w(tag, message, exception)
        }
    }

    override fun e(tag: String, message: String, exception: Throwable?) {
        instances.forEach {
            it.e(tag, message, exception)
        }
    }
}
