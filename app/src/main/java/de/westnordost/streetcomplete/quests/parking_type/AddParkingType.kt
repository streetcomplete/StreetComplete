package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags

class AddParkingType : OsmFilterQuestType<ParkingType>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = parking
          and (!parking or parking = yes)
    """
    override val changesetComment = "Specify parking types"
    override val wikiLink = "Tag:amenity=parking"
    override val icon = R.drawable.ic_quest_parking
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parkingType_title

    override fun createForm() = AddParkingTypeForm()

    override fun applyAnswerTo(answer: ParkingType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["parking"] = answer.osmValue
    }
}
