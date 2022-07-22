package de.westnordost.streetcomplete.quests.camera_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags

class AddCameraType : OsmFilterQuestType<CameraType>() {

    override val elementFilter = """
        nodes with
         surveillance:type = camera
         and surveillance ~ public|outdoor|traffic
         and !camera:type
    """
    override val changesetComment = "Specify camera types"
    override val wikiLink = "Tag:surveillance:type"
    override val icon = R.drawable.ic_quest_surveillance_camera
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_camera_type_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with surveillance and surveillance:type = camera")

    override fun createForm() = AddCameraTypeForm()

    override fun applyAnswerTo(answer: CameraType, tags: Tags, timestampEdited: Long) {
        tags["camera:type"] = answer.osmValue
    }
}
