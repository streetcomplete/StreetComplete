package de.westnordost.streetcomplete.quests.kerb_height

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.image_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddKerbHeightForm : AImageListQuestForm<KerbHeight, KerbHeight>() {

    override val items = KerbHeight.entries
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    @Composable override fun BoxScope.ItemContent(item: KerbHeight) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<KerbHeight>) {
        applyAnswer(selectedItems.single())
    }
}
