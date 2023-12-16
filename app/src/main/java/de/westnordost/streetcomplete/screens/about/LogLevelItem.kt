package de.westnordost.streetcomplete.screens.about

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogLevel

val LogLevel.styleResId: Int get() = when (this) {
    LogLevel.VERBOSE -> R.style.TextAppearance_LogMessage_Verbose
    LogLevel.DEBUG -> R.style.TextAppearance_LogMessage_Debug
    LogLevel.INFO -> R.style.TextAppearance_LogMessage_Info
    LogLevel.WARNING -> R.style.TextAppearance_LogMessage_Warning
    LogLevel.ERROR -> R.style.TextAppearance_LogMessage_Error
}

val LogLevel.colorId: Int get() = when (this) {
    LogLevel.VERBOSE -> R.color.log_verbose
    LogLevel.DEBUG -> R.color.log_debug
    LogLevel.INFO -> R.color.log_info
    LogLevel.WARNING -> R.color.log_warning
    LogLevel.ERROR -> R.color.log_error
}
