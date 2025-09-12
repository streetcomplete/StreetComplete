package de.westnordost.streetcomplete.quests.recycling

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRecyclingTypeForm : AImageListQuestForm<RecyclingType, RecyclingType>() {

    override val items = RecyclingType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: RecyclingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<RecyclingType>) {
        applyAnswer(selectedItems.single())
    }
}
