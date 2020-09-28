package de.westnordost.streetcomplete.quests.handrail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddHandrail(overpassApi: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Boolean>(overpassApi) {

    override val tagFilters = """
        ways with highway = steps
         and access !~ private|no
         and (!conveying or conveying = no)
         and (
           !handrail
           or handrail = no and handrail older today -${r * 4} years
           or handrail older today -${r * 8} years
         )
    """

    override val commitMessage = "Add whether steps have a handrail"
    override val wikiLink = "Key:handrail"
    override val icon = R.drawable.ic_quest_steps_handrail

    override fun getTitle(tags: Map<String, String>) = R.string.quest_handrail_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("handrail", if (answer) "yes" else "no")
    }
}
