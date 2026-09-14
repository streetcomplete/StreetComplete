package de.westnordost.streetcomplete.testutils

import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.seconds

/** Runs [observe] (typically a state holder's observer loop) in the background while [block]
 *  runs, e.g. to test a class that reacts to snapshot state via snapshotFlow. */
fun observing(observe: suspend () -> Unit, block: suspend () -> Unit) = runBlocking {
    val job = launch(Dispatchers.Default) { observe() }
    block()
    job.cancel()
}

/** Waits until the [condition] holds. Snapshot state written outside of a composition is only
 *  observed once its changes are applied, which this triggers while waiting. */
suspend fun awaitSnapshot(condition: () -> Boolean) = withTimeout(5.seconds) {
    while (!condition()) {
        Snapshot.sendApplyNotifications()
        delay(10)
    }
}
