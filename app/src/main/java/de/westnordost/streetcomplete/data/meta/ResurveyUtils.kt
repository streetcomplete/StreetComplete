package de.westnordost.streetcomplete.data.meta

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
fun String.toCheckDate(): Date? = OSM_CHECK_DATE_FORMAT.parseOrNull(this)

private fun SimpleDateFormat.parseOrNull(source: String): Date? =
    try { parse(source) } catch (e: ParseException) { null }

/** Date format of the tags used for recording the date at which the element or tag with the given
 *  key should be checked again. */
private val OSM_CHECK_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)