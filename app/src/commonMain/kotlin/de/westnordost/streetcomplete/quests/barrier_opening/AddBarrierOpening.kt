package de.westnordost.streetcomplete.quests.barrier_opening

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
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
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.measure.ArSupportChecker
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
            highway ~ ${ALL_PATHS.joinToString("|")}
            and area != yes
            and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    override val changesetComment = "Specify width of opening"
    override val wikiLink = "Key:barrier"
    override val icon = Res.drawable.quest_wheelchair_width
    override val title = Res.string.quest_barrier_opening_width_gate
    override val achievements = listOf(BICYCLIST, WHEELCHAIR)
    override val defaultDisabledMessage: StringResource?
        get() = if (!checkArSupport()) Res.string.default_disabled_msg_no_ar else null

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val wayNodeIds = mapData.ways
            .filter { waysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { it.id in wayNodeIds && nodeFilter.matches(it) }
    }

    override fun isApplicableTo(element: Element) =
        if (nodeFilter.matches(element)) null else false

    @Composable
    override fun Form(onAnswer: (WidthAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        val isSomeKindOfBollard =
            element.tags["barrier"] == "bollard" ||
            element.tags["barrier"] == "block" ||
            element.tags["cycle_barrier"] == "diagonal"

        AddWidthForm(
            onAnswer = onAnswer,
            element = element,
            title = stringResource(
                if (isSomeKindOfBollard) Res.string.quest_barrier_opening_width_bollard
                else Res.string.quest_barrier_opening_width_gate
            ),
            countryInfo = countryInfo,
        )
    }

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
