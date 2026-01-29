package de.westnordost.streetcomplete.util.locale

import java.time.format.FormatStyle

fun DateTimeFormatStyle.toFormatStyle(): FormatStyle = when (this) {
    DateTimeFormatStyle.Short -> FormatStyle.SHORT
    DateTimeFormatStyle.Medium -> FormatStyle.MEDIUM
    DateTimeFormatStyle.Long -> FormatStyle.LONG
    DateTimeFormatStyle.Full -> FormatStyle.FULL
}
