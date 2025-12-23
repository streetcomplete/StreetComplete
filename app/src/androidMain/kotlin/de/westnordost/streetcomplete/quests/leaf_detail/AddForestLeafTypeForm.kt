package de.westnordost.streetcomplete.quests.leaf_detail

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddForestLeafTypeForm : AItemSelectQuestForm<ForestLeafType, ForestLeafType>() {

    override val items = ForestLeafType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<ForestLeafType>()

    @Composable override fun ItemContent(item: ForestLeafType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: ForestLeafType) {
        applyAnswer(selectedItem)
    }
}
