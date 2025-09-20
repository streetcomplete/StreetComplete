package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleBarrierTypeForm :
    AItemSelectQuestForm<BicycleBarrierType, BicycleBarrierTypeAnswer>() {

    override val items = BicycleBarrierType.entries
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_barrier_bicycle_type_not_cycle_barrier) {
            applyAnswer(BarrierTypeIsNotBicycleBarrier)
        },
    )

    @Composable override fun ItemContent(item: BicycleBarrierType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: BicycleBarrierType) {
        applyAnswer(selectedItem)
    }
}
