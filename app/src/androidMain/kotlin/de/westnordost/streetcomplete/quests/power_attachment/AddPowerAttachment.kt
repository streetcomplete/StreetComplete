package de.westnordost.streetcomplete.quests.power_attachment

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BUILDING
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*

class AddPowerAttachment : OsmElementQuestType<PowerAttachment>, AndroidQuest {

    private val polesFilter by lazy { """
        nodes with
          power ~ tower|pole|insulator
          and !line_attachment
          and disused != yes
          and ruined != yes
          and abandoned != yes
    """.toElementFilterExpression() }

    private val powerLinesFilter by lazy { """
        ways with power ~ line|minor_line
    """.toElementFilterExpression() }

    override val changesetComment = "Specify line_attachment power support"
    override val wikiLink = "Key:line_attachment"
    override val icon = R.drawable.quest_power
    override val title = Res.string.quest_powerAttachment_title
    override val achievements = listOf(BUILDING)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        // when several power lines are attached to any one power pole, it is very likely that that
        // power pole has *multiple* power line attachments, which requires a more complex tagging
        // that the UI for this quest does not support. Hence, we filter out all power poles that
        // are nodes of more than one power lines. See #6547
        val poleNodeIdsWithSeveralPowerLines = mapData.ways
            .filter { powerLinesFilter.matches(it) }
            .flatMap { it.nodeIds }
            .groupingBy { it }.eachCount()
            .filter { (id, count) -> count != 1 }
            .mapTo(HashSet<Long>()) { (id, count) -> id }

        return mapData.nodes.filter {
            polesFilter.matches(it) && it.id !in poleNodeIdsWithSeveralPowerLines
        }
    }

    override fun isApplicableTo(element: Element): Boolean? {
        if (!polesFilter.matches(element)) return false
        // otherwise, we can't say, because the power pole may be part of several power lines
        // (see comment in getApplicableElements)
        return null
    }

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        // and also show the (power) lines themselves
        mapData.filter("nodes with power ~ tower|pole|insulator") +
        mapData.filter("ways with power ~ line|minor_line")

    // map data density is usually lower where there are power poles and more context is necessary
    // when looking at them from afar
    override val highlightedElementsRadius get() = 100.0

    override fun createForm() = AddPowerAttachmentForm()

    override fun applyAnswerTo(answer: PowerAttachment, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["line_attachment"] = answer.osmValue
    }
}
