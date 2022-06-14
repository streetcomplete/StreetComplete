package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.SURVEY_MARK_KEY

fun removeTagsDescribingConstruction(tags: Tags) {
    tags.remove("construction")
    tags.remove("source:construction")
    tags.remove("opening_date")
    tags.remove("source:opening_date")
    tags.remove(SURVEY_MARK_KEY)
    tags.remove("source:$SURVEY_MARK_KEY")
}
