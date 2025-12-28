package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.quest.AndroidQuest
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.BACKWARD
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.FORWARD
import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.NO_ONEWAY

class AddOnewayAerialway : OsmElementQuestType<OnewayAnswer>, AndroidQuest {

    private val elementFilter by lazy { """
        ways with
        aerialway ~ gondola|mixed_lift|chair_lift|t-bar|j-bar|platter
        and !oneway
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether aerial ways can be used both ways"
    override val wikiLink = "Key:oneway"
    override val icon = R.drawable.quest_aerialway_oneway
    override val hasMarkersAtEnds = true
    override val achievements = listOf(EditTypeAchievement.PEDESTRIAN)

    override val hint = R.string.quest_arrow_tutorial

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bothway_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> = mapData.ways.filter { elementFilter.matches(it) }

    override fun isApplicableTo(element: Element): Boolean? = elementFilter.matches(element)

    override fun createForm() = AddOnewayForm()

    override fun applyAnswerTo(answer: OnewayAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags["oneway"] = when (answer) {
            FORWARD -> "yes"
            BACKWARD -> "-1"
            NO_ONEWAY -> "no"
        }
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("""
            nodes, ways with aerialway
        """.toElementFilterExpression())
}
