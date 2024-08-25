package de.westnordost.streetcomplete.quests.barrier_opening

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.width.AddWidthForm
import de.westnordost.streetcomplete.quests.width.WidthAnswer
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker

class AddBarrierOpening(
    private val checkArSupport: ArSupportChecker
) : OsmElementQuestType<WidthAnswer> {

    private val nodeFilter by lazy { """
        nodes with
            (
                barrier ~ gate|entrance|sliding_gate|swing_gate|wicket_gate|bollard|block
                or barrier = cycle_barrier and cycle_barrier ~ single|diagonal
            )
            and (!maxwidth:physical or source:maxwidth_physical ~ ".*estimat.*")
            and (!width or source:width ~ ".*estimat.*")
            and (!maxwidth or source:maxwidth ~ ".*estimat.*")
            and access !~ private|no|customers|agricultural
        """.toElementFilterExpression() }

    private val waysFilter by lazy { """
        ways with
            highway
            and area != yes
            and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    override val changesetComment = "Specify width of opening"
    override val wikiLink = "Key:barrier"
    override val icon = R.drawable.ic_quest_wheelchair_width
    override val achievements = listOf(BICYCLIST, WHEELCHAIR)
    override val defaultDisabledMessage: Int
        get() = if (!checkArSupport()) R.string.default_disabled_msg_no_ar else 0

    override fun getTitle(tags: Map<String, String>) =
        if (tags["barrier"] == "bollard" || tags["barrier"] == "block" || tags["cycle_barrier"] == "diagonal") {
            R.string.quest_barrier_opening_width_bollard
        } else {
            R.string.quest_barrier_opening_width_gate
        }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val wayNodeIds = mapData.ways
            .filter { waysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { it.id in wayNodeIds && nodeFilter.matches(it) }
    }

    override fun isApplicableTo(element: Element) =
        if (nodeFilter.matches(element)) null else false

    override fun createForm() = AddWidthForm()

    override fun applyAnswerTo(answer: WidthAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        val key = "maxwidth:physical"

        tags[key] = answer.width.toOsmValue()

        if (answer.isARMeasurement) {
            tags["source:$key"] = "ARCore"
        } else {
            tags.remove("source:$key")
        }
    }
}
