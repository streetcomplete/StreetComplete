package de.westnordost.streetcomplete.quests.construction

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore
import java.util.*

class MarkCompletedBuildingConstruction(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        ways with building = construction
         and (!opening_date or opening_date < today)
         and older today -${r * 6} months
    """
    override val commitMessage = "Determine whether construction is now completed"
    override val wikiLink = "Tag:building=construction"
    override val icon = R.drawable.ic_quest_building_construction

    override fun getTitle(tags: Map<String, String>) = R.string.quest_construction_building_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        if (answer) {
            val value = changes.getPreviousValue("construction") ?: "yes"
            changes.modify("building", value)
            deleteTagsDescribingConstruction(changes)
        } else {
            changes.addOrModify(SURVEY_MARK_KEY, Date().toCheckDateString())
        }
    }
}
