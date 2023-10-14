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
