package de.westnordost.streetcomplete.util.error_reporting

import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString

/** Exception handler that takes care of storing the last crash as a file.
 *  When a crash occurs, the stack trace is saved to [crashReportFile] so that it can be accessed
 *  on next startup */
class CrashReportsUncaughtExceptionHandler(
    private val errorReportBuilder: ErrorReportBuilder,
    private val crashReportFile: Path
) : Thread.UncaughtExceptionHandler, CrashReportHolder {

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun install() {
        val ueh = Thread.getDefaultUncaughtExceptionHandler()
        check(ueh !is CrashReportsUncaughtExceptionHandler) { "May not install several CrashReportsUncaughtExceptionHandler!" }
        defaultUncaughtExceptionHandler = ueh
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        val report = errorReportBuilder.createErrorReport(error, thread.name)

        saveCrashReport(report)
        // without a default handler, the JVM would just print the stack trace
        defaultUncaughtExceptionHandler?.uncaughtException(thread, error)
            ?: error.printStackTrace()
    }

    override fun takeCrashReport(): String? {
        if (hasCrashReport()) {
            val errorReport = loadCrashReport()
            deleteCrashReport()
            return errorReport
        } else {
            return null
        }
    }

    private fun saveCrashReport(text: String) {
        try {
            SystemFileSystem.sink(crashReportFile).buffered().use { it.writeString(text) }
        } catch (ignored: IOException) {
        }
    }

    private fun hasCrashReport(): Boolean = SystemFileSystem.exists(crashReportFile)

    private fun loadCrashReport(): String? {
        try {
            return SystemFileSystem.source(crashReportFile).buffered().use { it.readString() }
        } catch (ignore: IOException) {
        }
        return null
    }

    private fun deleteCrashReport() {
        SystemFileSystem.delete(crashReportFile, mustExist = false)
    }
}
