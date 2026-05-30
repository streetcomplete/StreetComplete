package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.PEDESTRIAN
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBarrierOnPath : OsmElementQuestType<BarrierType> {

    private val barrierFilter by lazy { """
        ways with
          barrier ~ wall|fence|hedge|guard_rail|city_wall
          and area != yes
          and highway !~ ${ALL_PATHS.joinToString("|")}
    """.toElementFilterExpression() }

    private val pathsFilter by lazy { """
        ways with
          highway ~ ${ALL_PATHS.joinToString("|")}
          and area != yes
          and (access !~ private|no or (foot and foot !~ private|no))
    """.toElementFilterExpression() }

    override val changesetComment = "Specify how paths and barriers intersect"
    override val wikiLink = "Key:barrier"
    override val icon = Res.drawable.quest_barrier_on_path
    override val title = Res.string.quest_barrier_path_intersection
    override val achievements = listOf(PEDESTRIAN, WHEELCHAIR, OUTDOORS)

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        detectWayBarrierIntersection(mapData, barrierFilter, pathsFilter)

    override fun isApplicableTo(element: Element): Boolean? =
        if (element !is Node || element.tags.isNotEmpty()) false else null

    @Composable
    override fun Form(onAnswer: (QuestAnswer<BarrierType>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = BarrierType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onAnswer = onAnswer,
        )
    }

    override fun applyAnswerTo(answer: BarrierType, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) =
        answer.applyTo(tags)
}
