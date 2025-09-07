package de.westnordost.streetcomplete.util.locale

import java.time.format.FormatStyle

fun DateFormatStyle.toFormatStyle(): FormatStyle = when (this) {
    DateFormatStyle.Short -> FormatStyle.SHORT
    DateFormatStyle.Medium -> FormatStyle.MEDIUM
    DateFormatStyle.Long -> FormatStyle.LONG
    DateFormatStyle.Full -> FormatStyle.FULL
}
