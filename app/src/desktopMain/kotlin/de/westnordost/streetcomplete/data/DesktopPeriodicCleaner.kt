package de.westnordost.streetcomplete.data

import dev.nucleusframework.scheduler.DesktopBootReceiver
import dev.nucleusframework.scheduler.DesktopTask
import dev.nucleusframework.scheduler.DesktopTaskScheduler
import dev.nucleusframework.scheduler.SchedulerConfig
import dev.nucleusframework.scheduler.TaskContext
import dev.nucleusframework.scheduler.TaskId
import dev.nucleusframework.scheduler.TaskRegistry
import dev.nucleusframework.scheduler.TaskRequest
import dev.nucleusframework.scheduler.TaskResult
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

/** Cleans old data daily like on Android, scheduled with the OS, so that it also happens while the
 *  app is closed. See https://nucleusframework.dev/en/docs/lifecycle/scheduler/
 *
 *  When run from Gradle as a plain java process (on Linux and Windows), there is no app executable
 *  the OS could start, so it is instead done once per app start, a minute after startup. */
class DesktopPeriodicCleaner(private val clean: () -> Job) : PeriodicCleaner {
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("Cleaner")
    )

    override fun enqueue() {
        val executable = appExecutable()
        if (executable != null) {
            // the OS starts the task with the current directory set elsewhere
            SchedulerConfig.executablePath = executable.absolutePath
            DesktopTaskScheduler.enqueue(TaskRequest.periodic(TASK_ID, 1.days))
        } else {
            scope.launch {
                delay(1.minutes)
                clean()
            }
        }
    }

    /** Runs the cleaning if the OS started the app for that. Returns whether it did. */
    fun runIfScheduled(args: Array<String>): Boolean {
        if (!DesktopBootReceiver.isSchedulerInvocation(args)) return false
        val registry = TaskRegistry.Builder()
            .register(TASK_ID) {
                object : DesktopTask {
                    override suspend fun doWork(context: TaskContext): TaskResult {
                        clean().join()
                        return TaskResult.Success
                    }
                }
            }
            .build()
        DesktopBootReceiver.handle(args, registry)
        return true
    }

    fun close() { scope.cancel() }

    /** The app's own executable, or null if the app is run as a plain java process */
    private fun appExecutable(): File? {
        val command = ProcessHandle.current().info().command().orElse(null) ?: return null
        return File(command).takeIf { it.nameWithoutExtension != "java" }
    }

    private companion object {
        val TASK_ID = TaskId("cleanup")
    }
}
