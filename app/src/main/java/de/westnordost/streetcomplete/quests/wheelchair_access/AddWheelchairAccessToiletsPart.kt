package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddWheelchairAccessToiletsPart(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {
    override val tagFilters = """
        nodes, ways, relations with name and toilets = yes and !toilets:wheelchair
    """
    override val commitMessage = "Add wheelchair access to toilets"
    override val icon = R.drawable.ic_quest_toilets_wheelchair
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_toiletsPart_title

    override fun createForm() = AddWheelchairAccessToiletsForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("toilets:wheelchair", answer)
    }
}
