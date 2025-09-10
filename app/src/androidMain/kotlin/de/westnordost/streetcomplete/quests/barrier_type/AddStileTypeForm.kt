package de.westnordost.streetcomplete.quests.barrier_type

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddStileTypeForm : AImageListQuestForm<StileTypeAnswer, StileTypeAnswer>() {

    override val items: List<StileTypeAnswer> = StileType.entries + ConvertedStile.entries
    override val itemsPerRow = 2

    @Composable override fun BoxScope.ItemContent(item: StileTypeAnswer) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<StileTypeAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
