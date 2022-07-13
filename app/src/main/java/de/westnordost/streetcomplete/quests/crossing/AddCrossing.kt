package de.westnordost.streetcomplete.quests.crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.findNodesAtCrossingsOf
import de.westnordost.streetcomplete.osm.isCrossing
import de.westnordost.streetcomplete.osm.updateWithCheckDate
import de.westnordost.streetcomplete.quests.kerb_height.AddKerbHeightForm
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight

class AddCrossing : OsmElementQuestType<KerbHeight> {

    private val roadsFilter by lazy { """
        ways with
          highway ~ trunk|trunk_link|primary|primary_link|secondary|secondary_link|tertiary|tertiary_link|unclassified|residential
          and area != yes
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    private val footwaysFilter by lazy { """
        ways with
          (highway ~ footway|steps or highway ~ path|cycleway and foot ~ designated|yes)
          and footway !~ sidewalk|crossing
          and area != yes
          and access !~ private|no
    """.toElementFilterExpression() }

    /* It is neither asked for sidewalks nor crossings (=separately mapped sidewalk infrastructure)
    *  because a "no" answer would require to also delete/adapt the crossing ways, rather than just
    *  tagging crossing=no on the vertex.
    *  See https://github.com/streetcomplete/StreetComplete/pull/2999#discussion_r681516203 */

    override val changesetComment = "Specify whether there are crossings at intersections of paths and roads"
    override val wikiLink = "Tag:highway=crossing"
    override val icon = R.drawable.ic_quest_pedestrian
    override val achievements = listOf(PEDESTRIAN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_crossing_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter { it.isCrossing() }.asSequence()

    override fun isApplicableTo(element: Element): Boolean? =
        if (element !is Node || element.tags.isNotEmpty()) false else null

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

    override fun createForm() = AddKerbHeightForm()

    override fun applyAnswerTo(answer: KerbHeight, tags: Tags, timestampEdited: Long) {
        tags.updateWithCheckDate("kerb", answer.osmValue)
        /* So, we don't assume there is a crossing here for kerb=no and kerb=raised.

           As most actual crossings will have at least lowered kerbs, this is a good indicator.

           When there is no kerb at all, it is likely that this is a situation where the footway
           or road drawn in OSM are just virtual, to connect the geometry. In other words, it may be
           just e.g. an asphalted area, which does not really classify as a crossing.
         */

        if (answer.osmValue in listOf("lowered", "flush")) {
            tags["highway"] = "crossing"
        }
    }
}
