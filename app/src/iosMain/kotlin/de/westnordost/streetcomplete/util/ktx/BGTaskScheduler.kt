package de.westnordost.streetcomplete.util.ktx

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.BackgroundTasks.BGTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import kotlin.coroutines.resume

/** Returns requests that are still queued, preserving the Objective-C element type. */
suspend fun BGTaskScheduler.pendingTaskRequests(): List<BGTaskRequest> =
    suspendCancellableCoroutine { continuation ->
        getPendingTaskRequestsWithCompletionHandler(
            completionHandler = { requests ->
                continuation.resume(requests.orEmpty().map { it as BGTaskRequest })
        })
    }
