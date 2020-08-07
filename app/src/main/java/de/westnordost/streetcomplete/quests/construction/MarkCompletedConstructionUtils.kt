package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.meta.dateDaysAgo
import de.westnordost.streetcomplete.data.meta.toLastCheckDateString
import java.util.*

fun getCurrentDateString(): String = Date().toLastCheckDateString()

fun deleteTagsDescribingConstruction(changes: StringMapChangesBuilder) {
    changes.deleteIfExists("construction")
    changes.deleteIfExists("source:construction")
    changes.deleteIfExists("opening_date")
    changes.deleteIfExists("source:opening_date")
    changes.deleteIfExists(SURVEY_MARK_KEY)
    changes.deleteIfExists("source:$SURVEY_MARK_KEY")
}

fun isNotInFuture(tagKey: String): String {
    val today = getCurrentDateString()
    return "(if:!is_date(t['$tagKey']) || date(t['$tagKey']) < date('$today'))"
}

fun hasRecentlyBeenEdited(daysAgo: Float): String {
    val date = dateDaysAgo(daysAgo).toLastCheckDateString()
    return "(newer: '$date')"
}