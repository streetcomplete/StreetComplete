package de.westnordost.streetcomplete.data.sync

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationState
import platform.UIKit.UIBackgroundTaskInvalid

/** Continue foreground work for as long as iOS permits, retrying expired work on next foreground. */
internal suspend fun runIosSync(name: String, block: suspend () -> Unit) = withContext(Dispatchers.Main) {
    do {
        awaitForeground()
        val expired = coroutineScope {
            val app = UIApplication.sharedApplication
            var taskId = UIBackgroundTaskInvalid
            var expired = false
            val work = launch(Dispatchers.IO, start = CoroutineStart.LAZY) { block() }

            fun endTask() {
                if (taskId != UIBackgroundTaskInvalid) {
                    app.endBackgroundTask(taskId)
                    taskId = UIBackgroundTaskInvalid
                }
            }

            taskId = app.beginBackgroundTaskWithName(name) {
                expired = true
                work.cancel()
                // An upload may be finishing non-cancellable bookkeeping. Do not wait for it
                // here: iOS requires the assertion to be released before this handler returns.
                endTask()
            }
            try {
                work.start()
                work.join()
            } finally {
                endTask()
            }
            expired
        }
        // Completed edits and areas are persisted by the workers. Only expiration retries here;
        // network/auth errors are reported normally and left to the user or AutoSyncer to retry.
    } while (expired)
}

private suspend fun awaitForeground() {
    callbackFlow {
        val center = NSNotificationCenter.defaultCenter
        val observer = center.addObserverForName(
            UIApplicationDidBecomeActiveNotification, null, NSOperationQueue.mainQueue
        ) { trySend(Unit) }
        if (UIApplication.sharedApplication.applicationState != UIApplicationState.UIApplicationStateBackground) {
            trySend(Unit)
        }
        awaitClose { center.removeObserver(observer) }
    }.first()
}
