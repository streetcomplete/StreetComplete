package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddStileTypeForm : AItemSelectQuestForm<StileTypeAnswer, StileTypeAnswer>() {

    override val items: List<StileTypeAnswer> = StileType.entries + ConvertedStile.entries
    override val itemsPerRow = 2
    override val serializer = serializer<StileTypeAnswer>()

    @Composable override fun ItemContent(item: StileTypeAnswer) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: StileTypeAnswer) {
        applyAnswer(selectedItem)
    }
}
