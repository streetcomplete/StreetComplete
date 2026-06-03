package de.westnordost.streetcomplete.quests.bollard_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CAR
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBollardType : OsmElementQuestType<BollardTypeAnswer> {

    private val bollardNodeFilter by lazy { """
        nodes with
          barrier = bollard
          and !bollard
    """.toElementFilterExpression() }

    private val waysFilter by lazy { """
        ways with
          highway and highway != construction
          and area != yes
    """.toElementFilterExpression() }

    override val changesetComment = "Specify bollard types"
    override val wikiLink = "Key:bollard"
    override val icon = Res.drawable.quest_no_cars
    override val title = Res.string.quest_bollard_type_title
    override val achievements = listOf(CAR, LIFESAVER)

    // exclude free-floating nodes
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> {
        val wayNodeIds = mapData.ways
            .filter { waysFilter.matches(it) }
            .flatMapTo(HashSet()) { it.nodeIds }

        return mapData.nodes
            .filter { bollardNodeFilter.matches(it) && it.id in wayNodeIds }
    }

    override fun isApplicableTo(element: Element): Boolean? =
        if (!bollardNodeFilter.matches(element)) false else null

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes with barrier = bollard")

    @Composable
    override fun Form(on: (QuestAction<BollardTypeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            on = on,
            items = BollardType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            otherAnswers = { listOf(
                AnswerItem(stringResource(Res.string.quest_bollard_type_not_bollard)) {
                    on(Answer(BarrierTypeIsNotBollard))
                },
            ) }
        )
    }

    override fun applyAnswerTo(answer: BollardTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BollardType -> tags["bollard"] = answer.osmValue
            BarrierTypeIsNotBollard -> tags["barrier"] = "yes"
        }
    }
}
