package de.westnordost.streetcomplete.data

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.minutes

// TODO desktop: there is no API on the JVM to schedule periodic work, so instead of cleaning daily
//      like on Android, it is done once per app start, delayed so that it doesn't run in parallel
//      to app startup.
//      Could use https://nucleusframework.dev/en/docs/lifecycle/scheduler/ for that later.
class DesktopPeriodicCleaner(private val clean: () -> Unit) : PeriodicCleaner {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineName("Cleaner"))

    override fun enqueue() {
        scope.launch {
            delay(1.minutes)
            clean()
        }
    }

    fun close() { scope.cancel() }
}
