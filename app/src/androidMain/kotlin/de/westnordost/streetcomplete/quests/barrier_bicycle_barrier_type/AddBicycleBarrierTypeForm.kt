package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleBarrierTypeForm :
    AImageListQuestForm<BicycleBarrierType, BicycleBarrierTypeAnswer>() {

    override val items = BicycleBarrierType.entries
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<BicycleBarrierType>) {
        applyAnswer(selectedItems.single())
    }

    @Composable override fun BoxScope.ItemContent(item: BicycleBarrierType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_barrier_bicycle_type_not_cycle_barrier) {
            applyAnswer(BarrierTypeIsNotBicycleBarrier)
        },
    )
}
