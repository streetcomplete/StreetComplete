package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.MILLISECOND

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
    val month = groups[2].toIntOrNull() ?: return null
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

/** adds or modifies the given tag. If the updated tag is the same as before, sets the check date
 *  tag to today instead. */
fun StringMapChangesBuilder.updateWithCheckDate(key: String, value: String) {
    val previousValue = getPreviousValue(key)
    if (previousValue == value) {
        updateCheckDateForKey(key)
    } else {
        addOrModify(key, value)
        deleteCheckDatesForKey(key)
    }
}

/** Set/update solely the check date to today for the given key */
fun StringMapChangesBuilder.updateCheckDateForKey(key: String) {
    addOrModify("$SURVEY_MARK_KEY:$key", Date().toCheckDateString())
    // remove old check date keys (except the one we want to set)
    getLastCheckDateKeys(key).forEach {
        if (it != "$SURVEY_MARK_KEY:$key") deleteIfExists(it)
    }
}

/** Delete any check date keys for the given key */
fun StringMapChangesBuilder.deleteCheckDatesForKey(key: String) {
    getLastCheckDateKeys(key).forEach { deleteIfExists(it) }
}

/** Date format of the tags used for recording the date at which the element or tag with the given
 *  key should be checked again. */
private val OSM_CHECK_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)
// not using date format because we want to be able to understand 2000-11 as well
private val OSM_CHECK_DATE_REGEX = Regex("([0-9]{4})-([0-9]{2})(?:-([0-9]{2}))?")