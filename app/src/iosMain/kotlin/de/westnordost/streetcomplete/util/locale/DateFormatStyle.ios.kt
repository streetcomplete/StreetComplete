package de.westnordost.streetcomplete.util.locale

import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle

fun DateTimeFormatStyle.toNSDateFormatterStyle(): ULong = when (this) {
    DateTimeFormatStyle.Short -> NSDateFormatterShortStyle
    DateTimeFormatStyle.Medium -> NSDateFormatterMediumStyle
    DateTimeFormatStyle.Long -> NSDateFormatterLongStyle
    DateTimeFormatStyle.Full -> NSDateFormatterFullStyle
}
