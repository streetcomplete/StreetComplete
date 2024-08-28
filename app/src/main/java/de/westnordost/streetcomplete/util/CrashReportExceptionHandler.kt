package de.westnordost.streetcomplete.util

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogsController
import de.westnordost.streetcomplete.data.logs.format
import de.westnordost.streetcomplete.util.ktx.minusInSystemTimeZone
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.ktx.sendEmail
import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
    private val logsController: LogsController,
    private val prefs: ObservableSettings,
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

    fun askUserToSendErrorReport(activityCtx: AppCompatActivity, @StringRes titleResourceId: Int, e: Exception) {
        activityCtx.lifecycleScope.launch {
            val reportText = withContext(Dispatchers.IO) { createErrorReport(e, null) }
            askUserToSendErrorReport(activityCtx, titleResourceId, reportText)
        }
    }

    private fun askUserToSendErrorReport(activityCtx: Activity, @StringRes titleResourceId: Int, reportText: String?) {
        val mailText = "Describe how to reproduce it here:\n\n\n\n$reportText"

        AlertDialog.Builder(activityCtx)
            .setTitle(activityCtx.resources.getString(titleResourceId).replace("StreetComplete", "SCEE"))
            .setMessage(R.string.crash_message)
            .setPositiveButton(R.string.crash_compose_email) { _, _ ->
                activityCtx.sendEmail(mailReportTo, "Error Report", mailText)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->
                activityCtx.toast("\uD83D\uDE22")
            }
            .setNeutralButton(android.R.string.copy) { _, _ ->
                val clip = ClipData.newPlainText("SCEE error message", reportText)
                (activityCtx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
            }
            .setCancelable(false)
            .show()
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        val report = createErrorReport(error, thread)

        writeCrashReportToFile(report)
        defaultUncaughtExceptionHandler!!.uncaughtException(thread, error)
    }

    private fun createErrorReport(error: Throwable, thread: Thread?): String {
        val stackTrace = StringWriter()
        error.printStackTrace(PrintWriter(stackTrace))

        val logText = readLogFromDatabase()

        var report = ""

        if (thread != null) {
            report += "Thread: ${thread.name}"
        }

        report += "\n" + """
        App version: ${BuildConfig.VERSION_NAME}
        Device: ${Build.BRAND}  ${Build.DEVICE}, Android ${Build.VERSION.RELEASE}
        Locale: ${Locale.getDefault()}

        Stack trace:

        """.trimIndent()

        report += stackTrace

        report += "\nLog:\n"
        report += logText

        return report
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

    private fun readLogFromDatabase(): String {
        if (prefs.getBoolean(Prefs.TEMP_LOGGER, false)) {
            val tooOld = systemTimeNow().toLocalDateTime(TimeZone.currentSystemDefault())
                .minusInSystemTimeZone(ApplicationConstants.DO_NOT_ATTACH_LOG_TO_CRASH_REPORT_AFTER, DateTimeUnit.MILLISECOND)
            return TempLogger.getLog().filter { it.time > tooOld }.joinToString("\n") { it.toString() }
        }
        val newLogTimestamp =
            nowAsEpochMilliseconds() - ApplicationConstants.DO_NOT_ATTACH_LOG_TO_CRASH_REPORT_AFTER

        return logsController
            .getLogs(newerThan = newLogTimestamp)
            .format()
    }
}
