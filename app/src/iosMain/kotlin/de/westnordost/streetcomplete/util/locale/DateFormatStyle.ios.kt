package de.westnordost.streetcomplete.util.locale

import platform.Foundation.NSDateFormatterFullStyle
import platform.Foundation.NSDateFormatterLongStyle
import platform.Foundation.NSDateFormatterMediumStyle
import platform.Foundation.NSDateFormatterShortStyle

fun DateFormatStyle.toNSDateFormatterStyle(): ULong = when (this) {
    DateFormatStyle.Short -> NSDateFormatterShortStyle
    DateFormatStyle.Medium -> NSDateFormatterMediumStyle
    DateFormatStyle.Long -> NSDateFormatterLongStyle
    DateFormatStyle.Full -> NSDateFormatterFullStyle
}
