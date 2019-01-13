package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.DateUtil

abstract class AMarkCompletedConstruction<T> : OsmElementQuestType<T> {

    protected open fun getCurrentDateString() =
        DateUtil.getCurrentDateString() + "T00:00:00Z"

    protected open fun getOffsetDateString(offset: Int) =
        DateUtil.getOffsetDateString(offset) + "T00:00:00Z"

    // Note that newer segment will ensure that any edit,
    // including adding or updating review marker like check_date or survey:date tags
    // will cause OSM elements to become ineligible for this quest for reviewIntervalInDays days.
    // It allows supporting check_date and any other survey markers without parsing of any tags.
    protected fun getQueryPart( key: String, nameOfGeneratedGroup: String, reviewIntervalInDays: Int) =
        "[" + key + "=construction]" +
        "(if:!is_date(t['opening_date']) || date(t['opening_date'])<date('" + getCurrentDateString() + "'))" +
        " -> .construction_with_unknown_state; " +
        getRecentlyEditedConstructionsQueryPart(key,reviewIntervalInDays) + " -> .recently_edited_construction;" +
        "(.construction_with_unknown_state; - .recently_edited_construction;) -> " + nameOfGeneratedGroup + ";"

    private fun getRecentlyEditedConstructionsQueryPart( key: String, reviewIntervalInDays: Int) =
        "(" +
        "way[" + key + "=construction](newer: '" + getOffsetDateString(-reviewIntervalInDays) + "');" +
        "relation[" + key + "=construction](newer: '" + getOffsetDateString(-reviewIntervalInDays) + "');" +
        ")"

    protected fun removeTagsDescribingConstruction(changes: StringMapChangesBuilder) {
        changes.deleteIfExists("construction")
        changes.deleteIfExists("source:construction")
        changes.deleteIfExists("opening_date")
        changes.deleteIfExists("source:opening_date")
        changes.deleteIfExists(OsmTaggings.SURVEY_MARK_KEY)
        changes.deleteIfExists("source:" + OsmTaggings.SURVEY_MARK_KEY)
    }
}
