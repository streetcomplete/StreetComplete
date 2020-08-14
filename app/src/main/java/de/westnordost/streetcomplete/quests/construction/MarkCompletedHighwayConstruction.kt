package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.ALL_ROADS
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore
import java.util.*

class MarkCompletedHighwayConstruction(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        ways with highway = construction
         and (!opening_date or opening_date < today)
         and older today -${r * 2} weeks
    """
    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:highway=construction"
    override val icon = R.drawable.ic_quest_road_construction
    override val hasMarkersAtEnds = true

    override fun getTitle(tags: Map<String, String>): Int {
        val isRoad = ALL_ROADS.contains(tags["construction"])
        val isCycleway = tags["construction"] == "cycleway"
        val isFootway = tags["construction"] == "footway"

        return when {
            isRoad -> R.string.quest_construction_road_title
            isCycleway -> R.string.quest_construction_cycleway_title
            isFootway -> R.string.quest_construction_footway_title
            else -> R.string.quest_construction_generic_title
        }
    }

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            val value = changes.getPreviousValue("construction") ?: "road"
            changes.modify("highway", value)
            deleteTagsDescribingConstruction(changes)
        } else {
            changes.addOrModify(SURVEY_MARK_KEY, Date().toCheckDateString())
        }
    }
}
