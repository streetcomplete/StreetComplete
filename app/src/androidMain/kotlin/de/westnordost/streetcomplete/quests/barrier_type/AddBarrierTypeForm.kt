package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBarrierTypeForm : AImageListQuestForm<BarrierType, BarrierType>() {

    override val items = BarrierType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: BarrierType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<BarrierType>) {
        applyAnswer(selectedItems.single())
    }
}
