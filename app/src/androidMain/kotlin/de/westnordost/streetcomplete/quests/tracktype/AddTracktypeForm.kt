package de.westnordost.streetcomplete.quests.tracktype

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddTracktypeForm : AItemSelectQuestForm<Tracktype, Tracktype>() {

    override val items = Tracktype.entries
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false
    override val serializer = serializer<Tracktype>()

    @Composable override fun ItemContent(item: Tracktype) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: Tracktype) {
        applyAnswer(selectedItem)
    }
}
