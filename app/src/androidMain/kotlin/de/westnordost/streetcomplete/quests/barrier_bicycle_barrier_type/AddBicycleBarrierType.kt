package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BICYCLIST
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.BLIND
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.ItemSelectQuestForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleBarrierType : OsmFilterQuestType<BicycleBarrierTypeAnswer>() {

    override val elementFilter = "nodes with barrier = cycle_barrier and !cycle_barrier"
    override val changesetComment = "Specify cycle barrier types"
    override val wikiLink = "Key:cycle_barrier"
    override val icon = R.drawable.quest_no_bicycles
    override val title = Res.string.quest_bicycle_barrier_type_title
    override val achievements = listOf(BLIND, WHEELCHAIR, BICYCLIST)

    @Composable
    override fun Form(onAnswer: (BicycleBarrierTypeAnswer) -> Unit, element: Element) {
        ItemSelectQuestForm(
            items = BicycleBarrierType.entries,
            itemContent = { ImageWithLabel(painterResource(it.icon), stringResource(it.title)) },
            onClickOk = onAnswer,
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_barrier_bicycle_type_not_cycle_barrier)) {
                    onAnswer(BarrierTypeIsNotBicycleBarrier)
                },
            )
        )
    }

    override fun applyAnswerTo(answer: BicycleBarrierTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is BicycleBarrierType -> tags["cycle_barrier"] = answer.osmValue
            BarrierTypeIsNotBicycleBarrier -> tags["barrier"] = "yes"
        }
    }
}
