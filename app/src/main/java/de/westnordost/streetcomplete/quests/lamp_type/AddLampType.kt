package de.westnordost.streetcomplete.quests.lamp_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.quests.seating.AddOutdoorSeatingTypeForm

class AddLampType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes with
          highway = street_lamp
          and !lamp_type
          and !light:method
    """
    override val changesetComment = "Add lamp type"
    override val defaultDisabledMessage = R.string.quest_lampType_disabled_msg
    override val wikiLink = "Key:lamp_type"
    override val icon = R.drawable.ic_quest_lamp_type
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_lampType_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes with highway = street_lamp")

    override fun createForm() = AddLampTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["lamp_type"] = answer
    }
}
