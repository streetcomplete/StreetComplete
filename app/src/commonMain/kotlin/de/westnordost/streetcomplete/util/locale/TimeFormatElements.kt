package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalTime

/** Time format symbols used in a locale */
data class TimeFormatElements(
    /** null if 12-hour-clock is not used in that locale */
    val clock12: Clock12Elements? = null,
    /** most locales use ":" */
    val hourSeparator: String = ":",
    /** Zero character (for padding) */
    val zero: Char = '0',
    /** Text before time */
    val before: String = "",
    /** Text after time */
    val after: String = "",
) {
    companion object {
        fun of(locale: Locale?): TimeFormatElements {
            val formatter = LocalTimeFormatter(
                locale = locale,
                style = DateTimeFormatStyle.Short
            )
            val regex = Regex("(?:(\\D*)\\h)?(\\d{1,2})(\\D+)(\\d)\\d(?:\\h(.*))?")
            val early = formatter.format(LocalTime(1, 0))
            val late = formatter.format(LocalTime(13, 0))
            var beforeAm = ""
            var afterAm = ""
            var beforePm = ""
            var afterPm = ""
            var is24HourClock = false
            var hourSeparator = ":"
            var zero = '0'
            var clock12Elements: Clock12Elements? = null

            regex.matchEntire(early)?.let { matchResult ->
                val values = matchResult.groupValues
                beforeAm = values[1]
                hourSeparator = values[3]
                zero = values[4].firstOrNull() ?: '0'
                afterAm = values[5]
            }
            regex.matchEntire(late)?.let { matchResult ->
                val values = matchResult.groupValues
                beforePm = values[1]
                is24HourClock = values[2].toIntOrNull() == 13
                afterPm = values[5]
            }
            if (!is24HourClock) {
                if (afterAm != afterPm) {
                    clock12Elements = Clock12Elements(afterAm, afterPm, false)
                    afterAm = ""
                    afterPm = ""
                } else if (beforeAm != beforePm) {
                    clock12Elements = Clock12Elements(beforeAm, beforePm, true)
                    beforeAm = ""
                    beforePm = ""
                }
            }
            val before = if (beforeAm == beforePm) beforeAm else ""
            val after = if (afterAm == afterPm) afterAm else ""

            return TimeFormatElements(clock12Elements, hourSeparator, zero, before, after)
        }
    }
}

data class Clock12Elements(
    /** symbol used for AM (before noon) */
    val am: String,
    /** symbol used for PM (after noon) */
    val pm: String,
    /** whether AM/PM is usually displayed before the time*/
    val isInFront: Boolean = false
)
