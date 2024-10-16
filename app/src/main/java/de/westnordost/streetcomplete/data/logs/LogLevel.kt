package de.westnordost.streetcomplete.data.logs

import de.westnordost.streetcomplete.data.logs.LogLevel.*

enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

fun LogLevel.toChar(): Char = when (this) {
    VERBOSE -> 'V'
    DEBUG -> 'D'
    INFO -> 'I'
    WARNING -> 'W'
    ERROR -> 'E'
}
