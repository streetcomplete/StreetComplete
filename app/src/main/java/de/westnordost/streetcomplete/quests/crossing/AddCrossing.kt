package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.findNodesAtCrossingsOf
import de.westnordost.streetcomplete.osm.isCrossing
import de.westnordost.streetcomplete.quests.crossing.CrossingAnswer.*

class AddCrossing : OsmElementQuestType<CrossingAnswer> {

    private val roadsFilter by lazy { """
        ways with
          highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential|busway
          and area != yes
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    private val footwaysFilter by lazy { """
        ways with
          (highway ~ footway|steps or highway ~ path|cycleway and foot ~ designated|yes)
          and area != yes
          and access !~ private|no
    """.toElementFilterExpression() }

    override val changesetComment = "Specify whether there are crossings at intersections of paths and roads"
    override val wikiLink = "Tag:highway=crossing"
    override val icon = R.drawable.ic_quest_pedestrian
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.isCrossing() }.asSequence()

    override fun isApplicableTo(element: Element): Boolean? =
        if (element is Node && element.tags.isEmpty()) null else false

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val barrierWays = mapData.ways.asSequence()
            .filter { roadsFilter.matches(it) }

        val movingWays = mapData.ways.asSequence()
            .filter { footwaysFilter.matches(it) }

        var crossings = findNodesAtCrossingsOf(barrierWays, movingWays, mapData)

        /* require all roads at a shared node to either have no sidewalk tagging or all of them to
         * have sidewalk tagging: If the sidewalk tagging changes at that point, it may be an
         * indicator that this is the transition point between separate sidewalk mapping and
         * sidewalk mapping on road-way. E.g.:
         * https://www.openstreetmap.org/node/1839120490 */
        val anySidewalk = setOf("both", "left", "right")

        crossings = crossings.filter { crossing ->
            crossing.barrierWays.all { it.tags["sidewalk"] in anySidewalk } ||
            crossing.barrierWays.all { it.tags["sidewalk"] !in anySidewalk }
        }
        return crossings.map { it.node }.filter { it.tags.isEmpty() }
    }

    override fun createForm() = AddCrossingForm()

    override fun applyAnswerTo(answer: CrossingAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            YES -> tags["highway"] = "crossing"
            NO -> tags["crossing"] = "informal"
            PROHIBITED -> tags["crossing"] = "no"
        }
    }
}
