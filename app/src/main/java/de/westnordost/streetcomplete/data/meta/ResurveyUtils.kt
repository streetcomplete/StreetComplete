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

val LAST_CHECK_DATE_KEYS = listOf(
    "check_date",
    "lastcheck",
    "last_checked",
    "survey:date",
    "survey_date"
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
    if (previousValue != value) {
        addOrModify(key, value)
    }
    /* if the value is changed, set the check date only if it has been set before. Behavior
    *  before v32.0 was to delete the check date. However, this destroys data that was
    *  previously collected by another surveyor - we don't want to destroy other people's data
    *  */
    if (previousValue == value || hasCheckDateForKey(key)) {
        updateCheckDateForKey(key)
    }
}

/** Set/update solely the check date to today for the given key, this also removes other less
 *  preferred check date keys. */
fun StringMapChangesBuilder.updateCheckDateForKey(key: String) {
    addOrModify("$SURVEY_MARK_KEY:$key", LocalDate.now().toCheckDateString())
    // remove old check date keys (except the one we want to set)
    getLastCheckDateKeys(key).forEach {
        if (it != "$SURVEY_MARK_KEY:$key") deleteIfExists(it)
    }
}

/** Set/update solely the check date for the entire item to today, this also removes other less
 *  preferred check date keys for the entire item. */
fun StringMapChangesBuilder.updateCheckDate() {
    addOrModify(SURVEY_MARK_KEY, LocalDate.now().toCheckDateString())
    deleteOtherCheckDates()
}

/** Delete solely the other check date for the entire item, don't touch SURVEY_MARK_KEY */
fun StringMapChangesBuilder.deleteOtherCheckDates() {
    // remove old check dates (except the one we want to set)
    LAST_CHECK_DATE_KEYS.forEach {
        if (it != SURVEY_MARK_KEY) deleteIfExists(it)
    }
}

/** Delete any check date for the entire item */
fun StringMapChangesBuilder.deleteCheckDates() {
    deleteIfExists(SURVEY_MARK_KEY)
    deleteOtherCheckDates()
}

/** Return whether a check date is set for the given key */
fun StringMapChangesBuilder.hasCheckDateForKey(key: String): Boolean =
    getLastCheckDateKeys(key).any { getPreviousValue(it) != null }

/** Delete any check date keys for the given key */
fun StringMapChangesBuilder.deleteCheckDatesForKey(key: String) {
    getLastCheckDateKeys(key).forEach { deleteIfExists(it) }
}

/** Date format of the tags used for recording the date at which the element or tag with the given
 *  key should be checked again. */
// not using date format because we want to be able to understand 2000-11 as well
private val OSM_CHECK_DATE_REGEX = Regex("([0-9]{4})-([0-9]{2})(?:-([0-9]{2}))?")
