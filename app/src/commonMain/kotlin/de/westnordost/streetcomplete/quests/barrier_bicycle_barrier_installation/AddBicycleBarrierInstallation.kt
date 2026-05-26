package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.LIFESAVER
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleBarrierInstallation : OsmFilterQuestType<BicycleBarrierInstallationAnswer>() {

    override val elementFilter = """
        nodes, ways with barrier = cycle_barrier
         and cycle_barrier
         and cycle_barrier != tilted
         and !cycle_barrier:installation
    """
    override val changesetComment = "Specify cycle barrier installation"
    override val wikiLink = "Key:cycle_barrier:installation"
    override val icon = Res.drawable.quest_no_bicycles_lockable
    override val title = Res.string.quest_bicycle_barrier_installation_title
    override val achievements = listOf(BICYCLIST, LIFESAVER)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.filter("nodes, ways with barrier = cycle_barrier")

    @Composable
    override fun Form(onAnswer: (BicycleBarrierInstallationAnswer) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ItemSelectQuestForm(
            items = BicycleBarrierInstallation.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_barrier_bicycle_type_not_cycle_barrier)) {
                    onAnswer(BarrierTypeIsNotBicycleBarrier)
                }
            )
        )
    }

    override fun applyAnswerTo(answer: BicycleBarrierInstallationAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BicycleBarrierInstallation -> tags["cycle_barrier:installation"] = answer.osmValue
            BarrierTypeIsNotBicycleBarrier -> tags["barrier"] = "yes"
        }
    }
}
