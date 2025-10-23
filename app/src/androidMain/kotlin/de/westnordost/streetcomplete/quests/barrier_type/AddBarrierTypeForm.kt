package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddBarrierTypeForm : AItemSelectQuestForm<BarrierType, BarrierType>() {

    override val items = BarrierType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<BarrierType>()

    @Composable override fun ItemContent(item: BarrierType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: BarrierType) {
        applyAnswer(selectedItem)
    }
}
