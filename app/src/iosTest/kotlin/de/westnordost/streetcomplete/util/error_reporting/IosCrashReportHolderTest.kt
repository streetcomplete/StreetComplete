package de.westnordost.streetcomplete.util.error_reporting

import de.westnordost.streetcomplete.data.logs.LogLevel
import de.westnordost.streetcomplete.data.logs.LogMessage
import de.westnordost.streetcomplete.data.logs.LogsSource
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertNull

class IosCrashReportHolderTest {
    private val file = Path(SystemTemporaryDirectory, "streetcomplete-last-crash-test.txt")

    @AfterTest
    fun tearDown() {
        SystemFileSystem.delete(file, mustExist = false)
    }

    @Test
    fun recordsAndConsumesReport() {
        val holder = IosCrashReportHolder(
            file,
            ErrorReportBuilder(EmptyLogsSource),
            SystemFileSystem,
        )

        holder.record(IllegalStateException("crash persistence probe"))

        val report = holder.takeCrashReport()!!
        assertContains(report, "Thread: Kotlin/Native")
        assertContains(report, "IllegalStateException: crash persistence probe")
        assertFalse(SystemFileSystem.exists(file))
        assertNull(holder.takeCrashReport())
    }
}

private object EmptyLogsSource : LogsSource {
    override fun getLogs(
        levels: Set<LogLevel>,
        messageContains: String?,
        newerThan: Long?,
        olderThan: Long?,
    ): List<LogMessage> = emptyList()

    override fun addListener(listener: LogsSource.Listener) = Unit
    override fun removeListener(listener: LogsSource.Listener) = Unit
}
