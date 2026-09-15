package de.westnordost.streetcomplete.testutils

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/** Runs [observe] in the background while [block] runs */
fun observing(observe: suspend () -> Unit, block: suspend () -> Unit) = runBlocking {
    val job = launch(Dispatchers.Default) { observe() }
    block()
    job.cancel()
}

/** Waits until [condition] holds, applying pending snapshot changes meanwhile: writes outside a
 *  composition are only observed once applied. */
suspend fun awaitSnapshot(condition: () -> Boolean) = withTimeout(5.seconds) {
    while (!condition()) {
        Snapshot.sendApplyNotifications()
        delay(10)
    }
}
