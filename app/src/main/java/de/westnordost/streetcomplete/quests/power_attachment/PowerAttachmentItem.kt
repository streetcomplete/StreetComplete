package de.westnordost.streetcomplete.quests.power_attachment

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.power_attachment.PowerAttachment.*
import de.westnordost.streetcomplete.view.image_select.Item

fun PowerAttachment.asItem() = Item(this, iconResId, titleResId)

private val PowerAttachment.titleResId: Int get() = when (this) {
    SUSPENSION -> R.string.quest_powerAttachment_suspension
    ANCHOR -> R.string.quest_powerAttachment_anchor
    PIN -> R.string.quest_powerAttachment_pin
}

private val PowerAttachment.iconResId: Int get() = when (this) {
    SUSPENSION -> R.drawable.power_attachment_suspension
    ANCHOR -> R.drawable.power_attachment_anchor
    PIN -> R.drawable.power_attachment_pin
}
