package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.alternativeParsing
import kotlinx.datetime.format.char
import kotlinx.datetime.format.optional

/** Date-time format as specified in
 *  [section 3.3](https://www.rfc-editor.org/rfc/rfc2822#section-3.3) of RFC 2822 without the
 *  obsolete parts (defined in [section 4.3](https://www.rfc-editor.org/rfc/rfc2822#section-4.3)) */
val DateTimeComponents.Formats.RFC_2822_STRICT get() = rfc2822Format

private val rfc2822Format = DateTimeComponents.Format {
    alternativeParsing({}) {
        dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
        chars(", ")
    }
    dayOfMonth()
    char(' ')
    monthName(MonthNames.ENGLISH_ABBREVIATED)
    char(' ')
    year()
    char(' ')
    hour()
    char(':')
    minute()
    optional {
        char(':')
        second()
    }
    char(' ')
    offsetHours()
    offsetMinutesOfHour()
}
