package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.RARE
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddWheelchairAccessOutside : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways, relations with
         leisure = dog_park
         and access !~ no|private
         and (!wheelchair or wheelchair older today -8 years)
    """
    override val changesetComment = "Survey wheelchair accessibility of outside places"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_toilets_wheelchair
    override val achievements = listOf(RARE, WHEELCHAIR)

    override val hint = R.string.quest_wheelchairAccess_limited_description_outside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_outside_title

    override fun createForm() = WheelchairAccessForm()

    override fun applyAnswerTo(answer: WheelchairAccess, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateWithCheckDate("wheelchair", answer.osmValue)
    }
}
