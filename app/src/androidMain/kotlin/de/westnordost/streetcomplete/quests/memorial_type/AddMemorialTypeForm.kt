package de.westnordost.streetcomplete.quests.memorial_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddMemorialTypeForm : AItemSelectQuestForm<MemorialType, MemorialType>() {

    override val items = MemorialType.entries
    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: MemorialType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: MemorialType) {
        applyAnswer(selectedItem)
    }
}
