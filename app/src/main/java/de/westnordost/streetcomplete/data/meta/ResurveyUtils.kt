package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** Returns all the known keys used for recording the date at which the tag with the given key
 *  should be checked again. */
fun getLastCheckDateKeys(key: String): Sequence<String> = sequenceOf(
    "$key:check_date", "check_date:$key",
    "$key:lastcheck", "lastcheck:$key",
    "$key:last_checked", "last_checked:$key"
)

fun LocalDate.toCheckDateString(): String =
    DateTimeFormatter.ISO_LOCAL_DATE.format(this)

fun String.toCheckDate(): LocalDate? {
    val groups = OSM_CHECK_DATE_REGEX.matchEntire(this)?.groupValues ?: return null
    val year = groups[1].toIntOrNull() ?: return null
    val month = groups[2].toIntOrNull() ?: return null
    val day = groups[3].toIntOrNull() ?: 1

    return try {
        LocalDate.of(year, month, day)
    } catch (e: DateTimeException) {
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
    addOrModify("$SURVEY_MARK_KEY:$key", LocalDate.now().toCheckDateString())
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
// not using date format because we want to be able to understand 2000-11 as well
private val OSM_CHECK_DATE_REGEX = Regex("([0-9]{4})-([0-9]{2})(?:-([0-9]{2}))?")

val LAST_CHECK_DATE_KEYS = listOf(
    "check_date",
    "lastcheck",
    "last_checked",
    "survey:date",
    "survey_date"
)
