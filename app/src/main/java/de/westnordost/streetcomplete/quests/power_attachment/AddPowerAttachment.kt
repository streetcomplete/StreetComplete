package de.westnordost.streetcomplete.quests.power_attachment

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags

class AddPowerAttachment : OsmFilterQuestType<PowerAttachment>() {

    override val elementFilter = """
        nodes with
          power ~ tower|pole|insulator
          and !line_attachment
    """
    override val changesetComment = "Specify line_attachment power support"
    override val wikiLink = "Key:line_attachment"
    override val icon = R.drawable.ic_quest_power
    override val achievements = listOf(BUILDING)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_powerAttachment_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        val mapData = getMapData()
        // and also show the (power) lines themselves
        return mapData.filter("nodes with power ~ tower|pole|insulator") +
            mapData.filter("ways with power ~ line|minor_line")
    }

    // map data density is usually lower where there are power poles and more context is necessary
    // when looking at them from afar
    override val highlightedElementsRadius get() = 100.0

    override fun createForm() = AddPowerAttachmentForm()

    override fun applyAnswerTo(answer: PowerAttachment, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["line_attachment"] = answer.osmValue
    }
}
