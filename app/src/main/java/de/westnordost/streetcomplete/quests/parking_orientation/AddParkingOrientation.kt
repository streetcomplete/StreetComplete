package de.westnordost.streetcomplete.quests.parking_orientation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation

class AddParkingOrientation : OsmFilterQuestType<ParkingOrientation>() {

    override val elementFilter = """
        nodes, ways, relations with
          amenity = parking
          and parking ~ "lane|street_side|on_kerb|half_on_kerb"
          and !orientation
    """
    override val changesetComment = "Specify parking orientation"
    override val wikiLink = "Key:orientation"
    override val icon = R.drawable.ic_quest_parking_orientation
    override val achievements = listOf(CAR)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_parking_orientation_title

    override fun createForm() = AddParkingOrientationForm()

    override val defaultDisabledMessage = R.string.default_disabled_msg_ee

    override fun applyAnswerTo(answer: ParkingOrientation, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["orientation"] = answer.osmValue
    }
}
