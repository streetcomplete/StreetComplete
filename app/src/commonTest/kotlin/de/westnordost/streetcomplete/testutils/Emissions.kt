package de.westnordost.streetcomplete.testutils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** Collects the [flow] in the background into a queue, so that a test can await its emissions one
 *  by one with [next]. Call [stop] when done. */
fun <T> CoroutineScope.collectEmissions(flow: Flow<T>): Emissions<T> {
    val channel = Channel<T>(Channel.UNLIMITED)
    val job = launch(Dispatchers.Default) { flow.collect { channel.send(it) } }
    return Emissions(channel, job)
}

class Emissions<T>(private val channel: Channel<T>, private val job: Job) {
    suspend fun next(): T = channel.receive()
    fun stop() = job.cancel()
}
