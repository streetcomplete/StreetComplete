package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBicycleBarrierInstallationForm :
    AImageListQuestForm<BicycleBarrierInstallation, BicycleBarrierInstallationAnswer>() {

    override val items = BicycleBarrierInstallation.entries
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_barrier_bicycle_type_not_cycle_barrier) {
            applyAnswer(BarrierTypeIsNotBicycleBarrier)
        },
    )

    @Composable override fun BoxScope.ItemContent(item: BicycleBarrierInstallation) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BicycleBarrierInstallation>) {
        applyAnswer(selectedItems.single())
    }
}
