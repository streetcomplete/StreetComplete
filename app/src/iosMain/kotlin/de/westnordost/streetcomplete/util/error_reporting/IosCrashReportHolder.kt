package de.westnordost.streetcomplete.util.error_reporting

import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook

/** Persists the last unhandled Kotlin exception for the existing in-app crash dialog. */
class IosCrashReportHolder(
    private val file: Path,
    private val errorReportBuilder: ErrorReportBuilder,
    private val fileSystem: FileSystem,
) : CrashReportHolder {
    private var installed = false
    private var previousHandler: ((Throwable) -> Unit)? = null

    private val handler: (Throwable) -> Unit = { error ->
        record(error)
        previousHandler?.invoke(error) ?: error.printStackTrace()
    }

    @OptIn(ExperimentalNativeApi::class)
    fun install() {
        check(!installed) { "iOS crash report handler is already installed" }
        installed = true
        previousHandler = setUnhandledExceptionHook(handler)
    }

    internal fun record(error: Throwable) {
        try {
            fileSystem.sink(file).buffered().use {
                it.writeString(errorReportBuilder.createErrorReport(error, "Kotlin/Native"))
            }
        } catch (_: Throwable) {
            // An exception hook must never replace the original fatal error with a reporting
            // failure (for example, if database-backed report context is unavailable).
        }
    }

    override fun takeCrashReport(): String? {
        if (!fileSystem.exists(file)) return null
        return try {
            fileSystem.source(file).buffered().use { it.readString() }
                .also { fileSystem.delete(file, mustExist = false) }
        } catch (_: IOException) {
            null
        }
    }
}
