package de.westnordost.streetcomplete.data.sync

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One pending or running sync job, independent of the screen that requested it. */
internal class IosSyncJob(private val name: String) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main + CoroutineName(name))
    private val mutex = Mutex()
    private var job: Job? = null

    fun launch(replace: Boolean = false, block: suspend () -> Unit) {
        scope.launch {
            mutex.withLock {
                val previous = job
                if (replace) previous?.cancelAndJoin()
                else if (previous?.isActive == true) return@withLock

                job = scope.launch {
                    try {
                        runIosSync(name, block)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Downloader/Uploader already log the error and notify their listeners.
                    }
                }
            }
        }
    }

    fun close() {
        scope.cancel()
    }
}
