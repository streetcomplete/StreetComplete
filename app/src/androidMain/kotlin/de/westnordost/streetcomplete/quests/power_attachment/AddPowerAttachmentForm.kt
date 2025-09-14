package de.westnordost.streetcomplete.quests.power_attachment

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AItemSelectQuestForm
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

class AddPowerAttachmentForm : AItemSelectQuestForm<PowerAttachment, PowerAttachment>() {

    override val items = PowerAttachment.entries
    override val itemsPerRow = 3

    @Composable override fun ItemContent(item: PowerAttachment) {
        ImageWithLabel(painterResource(item.icon), stringResource(item.title))
    }

    override fun onClickOk(selectedItem: PowerAttachment) {
        applyAnswer(selectedItem)
    }
}
