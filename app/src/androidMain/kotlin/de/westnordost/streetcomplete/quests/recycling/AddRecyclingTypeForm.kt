package de.westnordost.streetcomplete.quests.recycling

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddRecyclingTypeForm : AItemSelectQuestForm<RecyclingType, RecyclingType>() {

    override val items = RecyclingType.entries
    override val itemsPerRow = 3
    override val serializer = serializer<RecyclingType>()

    @Composable override fun ItemContent(item: RecyclingType) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: RecyclingType) {
        applyAnswer(selectedItem)
    }
}
