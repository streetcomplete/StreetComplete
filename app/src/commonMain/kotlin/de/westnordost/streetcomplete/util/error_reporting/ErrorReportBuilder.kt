package de.westnordost.streetcomplete.util.error_reporting

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.logs.LogsSource
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds

/** Creates an error report including system information, recent log and stack trace */
class ErrorReportBuilder(
    private val logsSource: LogsSource
) {
    fun createErrorReport(
        error: Throwable? = null,
        threadName: String? = null
    ): String {
        val report = StringBuilder("")

        if (threadName != null) {
            report.append("Thread: $threadName")
        }

        report.append("""
            App version: ${BuildConfig.VERSION_NAME}
            Device: ${getDeviceSystemInfo()}
            Locale: ${Locale.current}
        """.trimIndent())

        if (error != null) {
            report.append("\nStack trace:\n")
            report.append(error.stackTraceToString())
        }

        report.append("\nLog:\n")
        report.append(readLogFromDatabase())

        return report.toString()
    }


    private fun readLogFromDatabase(): String {
        val newLogTimestamp =
            nowAsEpochMilliseconds() - ApplicationConstants.DO_NOT_ATTACH_LOG_TO_CRASH_REPORT_AFTER

        return logsSource
            .getLogs(newerThan = newLogTimestamp)
            .format()
    }
}
