package de.westnordost.streetcomplete.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.BuildConfig
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.toast
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton class CrashReportExceptionHandler @Inject constructor(
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
"""

        AlertDialog.Builder(activityCtx)
            .setTitle(titleResourceId)
            .setMessage(R.string.crash_message)
            .setPositiveButton(R.string.crash_compose_email) { _, _ ->
                sendEmail(activityCtx, report)
            }
            .setNegativeButton(android.R.string.no) { _, _ ->
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
"""
        )
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

    private fun sendEmail(activityCtx: Activity, text: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtras(bundleOf(
            Intent.EXTRA_EMAIL to arrayOf(mailReportTo),
            Intent.EXTRA_SUBJECT to ApplicationConstants.USER_AGENT + " Error Report",
            Intent.EXTRA_TEXT to text
        ))

        if (intent.resolveActivity(activityCtx.packageManager) != null) {
            activityCtx.startActivity(intent)
        } else {
            activityCtx.toast(R.string.no_email_client)
        }
    }
}