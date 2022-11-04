package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddWheelchairAccessToiletsPart : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways with
         toilets = yes
         and name
         and access !~ no|private
         and (
           !toilets:wheelchair
           or toilets:wheelchair != yes and toilets:wheelchair older today -4 years
           or toilets:wheelchair older today -8 years
         )
    """
    override val changesetComment = "Specify wheelchair accessibility of toilets in places"
    override val wikiLink = "Key:toilets:wheelchair"
    override val icon = R.drawable.ic_quest_toilets_wheelchair
    override val isReplaceShopEnabled = true
    override val achievements = listOf(RARE, WHEELCHAIR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_toiletsPart_title2

    override fun createForm() = AddWheelchairAccessToiletsForm()

    override fun applyAnswerTo(answer: WheelchairAccess, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("toilets:wheelchair", answer.osmValue)
    }
}
