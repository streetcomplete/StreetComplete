package de.westnordost.streetcomplete.quests.seating

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace

class AddOutdoorSeatingType : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
          outdoor_seating = yes
    """
    override val changesetComment = "Add outdoor seating info"
    override val defaultDisabledMessage = R.string.default_disabled_msg_seasonal
    override val wikiLink = "Key:outdoor_seating"
    override val icon = R.drawable.ic_quest_seating_type
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(EditTypeAchievement.CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_outdoor_seating_name_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlace() }

    override fun createForm() = AddOutdoorSeatingTypeForm()

    override fun applyAnswerTo(answer: String, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["outdoor_seating"] = answer
    }
}
