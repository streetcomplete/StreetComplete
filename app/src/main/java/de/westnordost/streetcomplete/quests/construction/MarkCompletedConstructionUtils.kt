package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import java.text.SimpleDateFormat
import java.util.*

private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.US)

fun getCurrentDateString(): String = DATE_FORMAT.format(Date())

fun deleteTagsDescribingConstruction(changes: StringMapChangesBuilder) {
    changes.deleteIfExists("construction")
    changes.deleteIfExists("source:construction")
    changes.deleteIfExists("opening_date")
    changes.deleteIfExists("source:opening_date")
    changes.deleteIfExists(SURVEY_MARK_KEY)
    changes.deleteIfExists("source:$SURVEY_MARK_KEY")
}

fun isNotInFuture(tagKey: String): String {
    val today = DATE_FORMAT.format(Date()) + "T00:00:00Z"
    return "(if:!is_date(t['$tagKey']) || date(t['$tagKey']) < date('$today'))"
}

fun hasRecentlyBeenEdited(daysAgo: Int): String {
    val cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_MONTH, -daysAgo)
    val date = DATE_FORMAT.format(cal.time) + "T00:00:00Z"
    return "(newer: '$date')"
}