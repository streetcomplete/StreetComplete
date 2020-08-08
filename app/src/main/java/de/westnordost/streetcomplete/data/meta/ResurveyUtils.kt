package de.westnordost.streetcomplete.data.meta

import java.lang.Exception
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.MILLISECOND

/** Returns the date x days in the past */
fun dateDaysAgo(daysAgo: Float): Date {
    val cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.SECOND, -(daysAgo * 24 * 60 * 60).toInt())
    return cal.time
}

/** Returns all the known keys used for recording the date at which the tag with the given key
 *  should be checked again. */
fun getLastCheckDateKeys(key: String): Sequence<String> = sequenceOf(
    "$key:check_date", "check_date:$key",
    "$key:lastcheck", "lastcheck:$key",
    "$key:last_checked", "last_checked:$key"
)

fun Date.toCheckDateString(): String = OSM_CHECK_DATE_FORMAT.format(this)
fun String.toCheckDate(): Date? {
    val groups = OSM_CHECK_DATE_REGEX.matchEntire(this)?.groupValues ?: return null
    val year = groups[1].toIntOrNull() ?: return null
    val month = groups[2].toIntOrNull() ?: 1
    val day = groups[3].toIntOrNull() ?: 1

    val calendar = Calendar.getInstance()
    return try {
        // -1 because this is the month index
        calendar.set(year, month-1, day, 0, 0, 0)
        calendar.set(MILLISECOND, 0)
        calendar.time
    } catch (e: Exception) {
        null
    }
}

private fun SimpleDateFormat.parseOrNull(source: String): Date? =
    try { parse(source) } catch (e: ParseException) { null }

/** Date format of the tags used for recording the date at which the element or tag with the given
 *  key should be checked again. */
private val OSM_CHECK_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
// not using date format because we want to be able to understand 2000 and 2000-11 as well
private val OSM_CHECK_DATE_REGEX = Regex("([0-9]{4})(?:-([0-9]{2})(?:-([0-9]{2}))?)?")