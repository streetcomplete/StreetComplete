package de.westnordost.streetcomplete.util.error_reporting

import android.content.Context
import kotlinx.io.IOException

/** Exception handler that takes care of storing the last crash as a file.
 *  When a crash occurs, the stack trace is saved to [crashReportFile] so that it can be accessed
 *  on next startup */
class CrashReportExceptionHandler(
    private val context: Context,
    private val errorReportBuilder: ErrorReportBuilder,
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
        val report = errorReportBuilder.createErrorReport(error, thread.name)

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
}
