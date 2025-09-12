package de.westnordost.streetcomplete.quests.power_attachment

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPowerAttachmentForm : AImageListQuestForm<PowerAttachment, PowerAttachment>() {

    override val items = PowerAttachment.entries
    override val itemsPerRow = 3

    @Composable override fun BoxScope.ItemContent(item: PowerAttachment) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItems: List<PowerAttachment>) {
        applyAnswer(selectedItems.single())
    }
}
