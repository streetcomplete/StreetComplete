package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddWheelchairAccessPublicTransport : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways, relations with
         (amenity = bus_station or railway ~ station|subway_entrance)
         and access !~ no|private
         and (
          !wheelchair
          or wheelchair != yes and wheelchair older today -4 years
          or wheelchair older today -8 years
         )
    """
    override val changesetComment = "Survey wheelchair accessibility of public transport platforms"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_wheelchair
    override val achievements = listOf(WHEELCHAIR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_outside_title

    override fun createForm() = AddWheelchairAccessPublicTransportForm()

    override fun applyAnswerTo(answer: WheelchairAccess, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("wheelchair", answer.osmValue)
    }
}
