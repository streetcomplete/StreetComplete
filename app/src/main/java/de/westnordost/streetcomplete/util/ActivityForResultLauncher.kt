package de.westnordost.streetcomplete.util

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContract
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Launches an activity for result (suspending) */
class ActivityForResultLauncher<I, O> (
    caller: ActivityResultCaller,
    contract: ActivityResultContract<I, O>
) {
    private var continuation: CancellableContinuation<O>? = null
    private val launcher = caller.registerForActivityResult(contract) { continuation?.resume(it) }

    suspend operator fun invoke(input: I): O = suspendCancellableCoroutine {
        continuation?.cancel()
        continuation = it
        launcher.launch(input)
        it.invokeOnCancellation {
            continuation = null
        }
    }
}
