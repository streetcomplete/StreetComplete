package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddWheelchairAccessToiletsPart(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        nodes, ways, relations with name and toilets = yes
         and (
           !toilets:wheelchair
           or toilets:wheelchair != yes and toilets:wheelchair older today -${r * 4} years
           or toilets:wheelchair older today -${r * 8} years
         )
    """
    override val commitMessage = "Add wheelchair access to toilets"
    override val wikiLink = "Key:toilets:wheelchair"
    override val icon = R.drawable.ic_quest_toilets_wheelchair
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_toiletsPart_title

    override fun createForm() = AddWheelchairAccessToiletsForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("toilets:wheelchair", answer)
    }
}
