package de.westnordost.streetcomplete.quests.kerb_height

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddKerbHeightForm : AItemSelectQuestForm<KerbHeight, KerbHeight>() {

    override val items = KerbHeight.entries
    override val itemsPerRow = 2
    override val serializer = serializer<KerbHeight>()
    override val moveFavoritesToFront = false

    @Composable override fun ItemContent(item: KerbHeight) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: KerbHeight) {
        applyAnswer(selectedItem)
    }
}
