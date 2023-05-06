package de.westnordost.streetcomplete.util

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.sendEmail
import de.westnordost.streetcomplete.util.ktx.toast
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Locale

/** Exception handler that takes care of asking the user to send the report of the last crash
 *  to the email address [mailReportTo].
 *  When a crash occurs, the stack trace is saved to [crashReportFile] so that it can be accessed
 *  on next startup */
class CrashReportExceptionHandler(
    private val appCtx: Context,
    private val mailReportTo: String,
    private val crashReportFile: String
) : Thread.UncaughtExceptionHandler {

    private var defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    fun install(): Boolean {
        val installerPackageName = appCtx.packageManager.getInstallerPackageName(appCtx.packageName)
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

    fun askUserToSendCrashReportIfExists(activityCtx: Activity) {
        if (hasCrashReport()) {
            val reportText = readCrashReportFromFile()
            deleteCrashReport()
            askUserToSendErrorReport(activityCtx, R.string.crash_title, reportText)
        }
    }

    fun askUserToSendErrorReport(activityCtx: Activity, @StringRes titleResourceId: Int, e: Exception) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        askUserToSendErrorReport(activityCtx, titleResourceId, stackTrace.toString())
    }

    private fun askUserToSendErrorReport(activityCtx: Activity, @StringRes titleResourceId: Int, error: String?) {
        val report = """
        Describe how to reproduce it here:



        $error
        """.trimIndent()

        AlertDialog.Builder(activityCtx)
            .setTitle(titleResourceId)
            .setMessage(R.string.crash_message)
            .setPositiveButton(R.string.crash_compose_email) { _, _ ->
                activityCtx.sendEmail(mailReportTo, "Error Report", report)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                activityCtx.toast("\uD83D\uDE22")
            }
            .setCancelable(false)
            .show()
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        val stackTrace = StringWriter()
        e.printStackTrace(PrintWriter(stackTrace))
        writeCrashReportToFile("""
        Thread: ${t.name}
        App version: ${BuildConfig.VERSION_NAME}
        Device: ${Build.BRAND}  ${Build.DEVICE}, Android ${Build.VERSION.RELEASE}
        Locale: ${Locale.getDefault()}
        Stack trace:
        $stackTrace
        """.trimIndent())
        defaultUncaughtExceptionHandler!!.uncaughtException(t, e)
    }

    private fun writeCrashReportToFile(text: String) {
        try {
            appCtx.openFileOutput(crashReportFile, Context.MODE_PRIVATE).bufferedWriter().use { it.write(text) }
        } catch (ignored: IOException) {
        }
    }

    private fun hasCrashReport(): Boolean = appCtx.fileList().contains(crashReportFile)

    private fun readCrashReportFromFile(): String? {
        try {
            return appCtx.openFileInput(crashReportFile).bufferedReader().use { it.readText() }
        } catch (ignore: IOException) {
        }
        return null
    }

    private fun deleteCrashReport() {
        appCtx.deleteFile(crashReportFile)
    }
}
