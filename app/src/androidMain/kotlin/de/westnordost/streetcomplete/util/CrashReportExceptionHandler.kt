package de.westnordost.streetcomplete.util

import android.content.Context
import android.os.Build
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import kotlinx.io.IOException
import java.util.Locale

/** Exception handler that takes care of storing the last crash as a file.
 *  When a crash occurs, the stack trace is saved to [crashReportFile] so that it can be accessed
 *  on next startup */
class CrashReportExceptionHandler(
    private val context: Context,
    private val logsController: LogsController,
    private val crashReportFile: String
) : Thread.UncaughtExceptionHandler {

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun install(): Boolean {
        val installerPackageName = context.packageManager.getInstallerPackageName(context.packageName)
        // developer. Don't need this functionality (it might even interfere with unit tests)
        if (installerPackageName == null) return false
        // don't need this for google play users: they have their own crash reports
        if (installerPackageName == "com.android.vending") return false
        val ueh = Thread.getDefaultUncaughtExceptionHandler()
        check(ueh !is CrashReportExceptionHandler) { "May not install several CrashReportExceptionHandlers!" }
        defaultUncaughtExceptionHandler = ueh
        Thread.setDefaultUncaughtExceptionHandler(this)
        return true
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        val report = createErrorReport(error, thread)

        saveCrashReport(report)
        defaultUncaughtExceptionHandler?.uncaughtException(thread, error)
    }

    fun popCrashReport(): String? {
        if (hasCrashReport()) {
            val errorReport = loadCrashReport()
            deleteCrashReport()
            return errorReport
        } else {
            return null
        }
    }

    fun createErrorReport(error: Throwable, thread: Thread? = null): String {
        val report = StringBuilder("")

        if (thread != null) {
            report.append("Thread: ${thread.name}")
        }

        report.append("""
            App version: ${BuildConfig.VERSION_NAME}
            Device: ${Build.BRAND}  ${Build.DEVICE}, Android ${Build.VERSION.RELEASE}
            Locale: ${Locale.getDefault()}

            Stack trace:

            """.trimIndent()
        )

        report.append(error.stackTraceToString())

        report.append("\nLog:\n")
        report.append(readLogFromDatabase())

        return report.toString()
    }

    private fun saveCrashReport(text: String) {
        try {
            context.openFileOutput(crashReportFile, Context.MODE_PRIVATE).bufferedWriter().use { it.write(text) }
        } catch (ignored: IOException) {
        }
    }

    private fun hasCrashReport(): Boolean = context.fileList().contains(crashReportFile)

    private fun loadCrashReport(): String? {
        try {
            return context.openFileInput(crashReportFile).bufferedReader().use { it.readText() }
        } catch (ignore: IOException) {
        }
        return null
    }

    private fun deleteCrashReport() {
        context.deleteFile(crashReportFile)
    }

    private fun readLogFromDatabase(): String {
        val newLogTimestamp =
            nowAsEpochMilliseconds() - ApplicationConstants.DO_NOT_ATTACH_LOG_TO_CRASH_REPORT_AFTER

        return logsController
            .getLogs(newerThan = newLogTimestamp)
            .format()
    }
}
