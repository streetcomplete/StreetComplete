package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

class AddWheelchairAccessToiletsPart : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways, relations with name and toilets = yes
         and (
           !toilets:wheelchair
           or toilets:wheelchair != yes and toilets:wheelchair older today -4 years
           or toilets:wheelchair older today -8 years
         )
    """
    override val commitMessage = "Add wheelchair access to toilets"
    override val wikiLink = "Key:toilets:wheelchair"
    override val icon = R.drawable.ic_quest_toilets_wheelchair
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_toiletsPart_title

    override fun createForm() = AddWheelchairAccessToiletsForm()

    override fun applyAnswerTo(answer: WheelchairAccess, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("toilets:wheelchair", answer.osmValue)
    }
}
