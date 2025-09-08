package de.westnordost.streetcomplete.quests.power_attachment

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddPowerAttachmentForm : AImageListQuestComposeForm<PowerAttachment, PowerAttachment>() {

    override val items = PowerAttachment.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<PowerAttachment>) {
        applyAnswer(selectedItems.single())
    }
}
