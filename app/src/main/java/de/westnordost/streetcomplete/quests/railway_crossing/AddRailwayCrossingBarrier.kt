package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.updateWithCheckDate

class AddRailwayCrossingBarrier : OsmElementQuestType<RailwayCrossingBarrier> {

    private val crossingFilter by lazy { """
        nodes with
          railway ~ level_crossing|crossing
          and (
            !crossing:barrier and !crossing:chicane
            or crossing:barrier older today -8 years
          )
    """.toElementFilterExpression() }

    private val excludedWaysFilter by lazy { """
        ways with
          highway and access ~ private|no
          or railway ~ tram|abandoned|disused
          or railway and embedded = yes
    """.toElementFilterExpression() }

    override val changesetComment = "Specify railway crossing barriers type"
    override val wikiLink = "Key:crossing:barrier"
    override val icon = R.drawable.ic_quest_railway
    override val achievements = listOf(CAR, PEDESTRIAN, BICYCLIST)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_railway_crossing_barrier_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val excludedWayNodeIds = mapData.ways
            .filter { excludedWaysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { crossingFilter.matches(it) && it.id !in excludedWayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!crossingFilter.matches(element)) false else null

    override fun createForm() = AddRailwayCrossingBarrierForm()

    override fun applyAnswerTo(answer: RailwayCrossingBarrier, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        if (answer.osmValue != null) {
            tags.remove("crossing:chicane")
            tags.updateWithCheckDate("crossing:barrier", answer.osmValue)
        }
        /* The mere existence of the crossing:chicane tag seems to imply that there could be a
         * barrier additionally to the chicane.
         * However, we still tag crossing:barrier=no here because the illustration as shown
         * in the app corresponds to the below tagging - it shows just a chicane and no further
         * barriers */
        if (answer == RailwayCrossingBarrier.CHICANE) {
            tags.updateWithCheckDate("crossing:barrier", "no")
            tags["crossing:chicane"] = "yes"
        }
    }
}
