package de.westnordost.streetcomplete.data.sync

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIBackgroundTaskInvalid
import platform.darwin.NSObjectProtocol
import kotlin.coroutines.resume

/** Continue foreground work for as long as iOS permits, retrying expired work on next foreground. */
internal suspend fun runIosSync(name: String, block: suspend () -> Unit) = withContext(Dispatchers.Main) {
    do {
        awaitForeground()
        val expired = coroutineScope {
            val app = UIApplication.sharedApplication
            var taskId = UIBackgroundTaskInvalid
            var expired = false
            val work = launch(start = CoroutineStart.LAZY) { block() }

            fun endTask() {
                if (taskId != UIBackgroundTaskInvalid) {
                    app.endBackgroundTask(taskId)
                    taskId = UIBackgroundTaskInvalid
                }
            }

            taskId = app.beginBackgroundTaskWithName(name, expirationHandler = {
                expired = true
                work.cancel()
                // End the background task before iOS's deadline. The finally below runs only after
                // work.join() returns or throws, which may be delayed by the current upload finishing.
                // https://developer.apple.com/documentation/uikit/uiapplication/beginbackgroundtask(withname:expirationhandler:)
                endTask()
            })
            try {
                work.start()
                work.join()
            } finally {
                endTask()
            }
            expired
        }
        // If background time expired, wait for foreground and call block() again.
        // Downloads retry the same area; uploads process the remaining unsynced edits.
    } while (expired)
}

/**
 * Work started in the foreground may still be running when the user locks the phone or switches
 * apps. Apple's [background task API] grants finite execution time and calls the expiration
 * handler before it runs out. Our handler cancels the work; this wait delays its retry until
 * the user returns. Apple's [activation notification] signals that the app is active again.
 * This retries interrupted work only while the process survives; it does not schedule new work
 * in the background.
 *
 * [background task API]: https://developer.apple.com/documentation/uikit/uiapplication/beginbackgroundtask(withname:expirationhandler:)
 * [activation notification]: https://developer.apple.com/documentation/uikit/uiapplication/didbecomeactivenotification
 */
private suspend fun awaitForeground() {
    if (UIApplication.sharedApplication.applicationState != UIApplicationState.UIApplicationStateBackground) return

    val center = NSNotificationCenter.defaultCenter
    var observer: NSObjectProtocol? = null
    try {
        suspendCancellableCoroutine<Unit> { continuation ->
            observer = center.addObserverForName(
                UIApplicationDidBecomeActiveNotification, null, NSOperationQueue.mainQueue
            ) {
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    } finally {
        observer?.let { center.removeObserver(it) }
    }
}
