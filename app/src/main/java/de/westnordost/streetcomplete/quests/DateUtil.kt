package de.westnordost.streetcomplete.quests

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtil {

    fun getCurrentDateString(): String = getOffsetDateString(0)

    fun basicISO8601() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * negative offsetInDays returns dates from past, positive from future
     */
    fun getOffsetDateStringFromDate(offsetInDays: Int, date: Date): String {
        val modifiedCalendar = Calendar.getInstance()
        modifiedCalendar.time = date
        modifiedCalendar.add(Calendar.DAY_OF_MONTH, offsetInDays)
        return basicISO8601().format(modifiedCalendar.time)
    }

    /**
     * negative offsetInDays returns dates from past, positive from future
     */
    fun getOffsetDateString(offsetInDays: Int) =
        getOffsetDateStringFromDate(offsetInDays, Calendar.getInstance().time)
}
