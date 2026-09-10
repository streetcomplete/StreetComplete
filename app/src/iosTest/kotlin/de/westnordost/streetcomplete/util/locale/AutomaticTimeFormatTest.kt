package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.currentLocale
import platform.Foundation.timeZoneForSecondsFromGMT
import kotlin.test.Test
import kotlin.test.assertEquals

/** Run with en-US and the system 24-hour override enabled to cover the reported regression. */
class AutomaticTimeFormatTest {
    @Test fun automaticFormattingRetainsNativePreferences() {
        val native = NSDateFormatter().apply {
            dateStyle = NSDateFormatterNoStyle
            timeStyle = NSDateFormatterShortStyle
            timeZone = NSTimeZone.timeZoneForSecondsFromGMT(0)
        }
        val formatter = LocalTimeFormatter(
            locale = null,
            timeZone = TimeZone.UTC,
            style = DateTimeFormatStyle.Short,
        )
        for (hour in listOf(1, 13)) {
            val date = NSDate(timeIntervalSinceReferenceDate = hour * 3600.0)
            assertEquals(native.stringFromDate(date), formatter.format(LocalTime(hour, 0)))
        }
    }

    @Test fun automaticPickerUsesNativeHourCycle() {
        val pattern = NSDateFormatter.dateFormatFromTemplate(
            "j", options = 0u, locale = NSLocale.currentLocale,
        )!!
        val hasDayPeriod = pattern.any { it == 'a' || it == 'b' || it == 'B' }
        assertEquals(hasDayPeriod, TimeFormatElements.of(null).clock12 != null)
    }

    @Test fun explicitLocaleRetainsItsOwnHourCycle() {
        assertEquals(Clock12Elements("AM", "PM"), TimeFormatElements.of(Locale("en-US")).clock12)
        assertEquals(null, TimeFormatElements.of(Locale("de-DE")).clock12)
    }
}
