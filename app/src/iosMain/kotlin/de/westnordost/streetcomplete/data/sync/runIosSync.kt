package de.westnordost.streetcomplete.data.sync

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.UIKit.UIApplication
import platform.UIKit.UIBackgroundTaskInvalid

/** Allow a download or upload to continue in the background until it finishes or iOS expires it. */
internal suspend fun runIosSync(name: String, block: suspend () -> Unit) = withContext(Dispatchers.Main) {
    val app = UIApplication.sharedApplication
    var taskId = UIBackgroundTaskInvalid
    val work = launch(start = CoroutineStart.LAZY) { block() }

    fun endTask() {
        if (taskId != UIBackgroundTaskInvalid) {
            app.endBackgroundTask(taskId)
            taskId = UIBackgroundTaskInvalid
        }
    }

    taskId = app.beginBackgroundTaskWithName(name, expirationHandler = {
        work.cancel()
        // End the background task before iOS's deadline. The finally below runs only after
        // work.join() returns or throws, which may be delayed by the current upload finishing.
        // https://developer.apple.com/documentation/uikit/uiapplication/beginbackgroundtask(withname:expirationhandler:)
        endTask()
    })
    try {
        work.join()
    } finally {
        endTask()
    }
}
