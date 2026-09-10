package de.westnordost.streetcomplete.util.error_reporting

import java.io.File
import java.io.IOException

/** Persists the last uncaught desktop exception for the existing in-app crash dialog. */
class DesktopCrashReportHolder(
    private val file: File,
    private val errorReportBuilder: ErrorReportBuilder,
) : CrashReportHolder, Thread.UncaughtExceptionHandler {
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    fun install() {
        val handler = Thread.getDefaultUncaughtExceptionHandler()
        check(handler !== this) { "Desktop crash report handler is already installed" }
        previousHandler = handler
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        try {
            file.parentFile.mkdirs()
            file.writeText(errorReportBuilder.createErrorReport(error, thread.name))
        } catch (_: IOException) {
        }
        previousHandler?.uncaughtException(thread, error) ?: error.printStackTrace()
    }

    override fun takeCrashReport(): String? {
        if (!file.isFile) return null
        return try {
            file.readText().also { file.delete() }
        } catch (_: IOException) {
            null
        }
    }
}
