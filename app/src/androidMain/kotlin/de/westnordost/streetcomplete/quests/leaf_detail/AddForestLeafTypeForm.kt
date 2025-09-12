package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddForestLeafTypeForm : AImageListQuestForm<ForestLeafType, ForestLeafType>() {

    override val items = ForestLeafType.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: ForestLeafType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<ForestLeafType>) {
        applyAnswer(selectedItems.single())
    }
}
