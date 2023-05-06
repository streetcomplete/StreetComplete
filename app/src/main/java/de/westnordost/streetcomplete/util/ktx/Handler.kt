package de.westnordost.streetcomplete.util.ktx

import android.os.Handler

/** run on main thread immediately if already on the thread the handler's looper runs on, otherwise
 *  post it there */
inline fun Handler.runImmediate(crossinline block: () -> Unit) {
    if (looper.thread == Thread.currentThread()) {
        block()
    } else {
        post { block() }
    }
}
