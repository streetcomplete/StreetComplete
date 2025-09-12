package de.westnordost.streetcomplete.quests.tracktype

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTracktypeForm : AImageListQuestForm<Tracktype, Tracktype>() {

    override val items = Tracktype.entries

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    @Composable override fun BoxScope.ItemContent(item: Tracktype) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<Tracktype>) {
        applyAnswer(selectedItems.single())
    }
}
