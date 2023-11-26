package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.logs.LogLevel.*

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

val LogLevel.styleResId: Int get() = when (this) {
    VERBOSE -> R.style.TextAppearance_LogMessage_Verbose
    DEBUG -> R.style.TextAppearance_LogMessage_Debug
    INFO -> R.style.TextAppearance_LogMessage_Info
    WARNING -> R.style.TextAppearance_LogMessage_Warning
    ERROR -> R.style.TextAppearance_LogMessage_Error
}

val LogLevel.colorId: Int get() = when (this) {
    VERBOSE -> R.color.log_verbose
    DEBUG -> R.color.log_debug
    INFO -> R.color.log_info
    WARNING -> R.color.log_warning
    ERROR -> R.color.log_error
}
