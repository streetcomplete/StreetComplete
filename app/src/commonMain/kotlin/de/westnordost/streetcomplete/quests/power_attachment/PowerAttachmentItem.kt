package de.westnordost.streetcomplete.quests.power_attachment

import de.westnordost.streetcomplete.quests.power_attachment.PowerAttachment.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val PowerAttachment.title: StringResource get() = when (this) {
    SUSPENSION -> Res.string.quest_powerAttachment_suspension
    ANCHOR -> Res.string.quest_powerAttachment_anchor
    PIN -> Res.string.quest_powerAttachment_pin
}

val PowerAttachment.icon: DrawableResource get() = when (this) {
    SUSPENSION -> Res.drawable.power_attachment_suspension
    ANCHOR -> Res.drawable.power_attachment_anchor
    PIN -> Res.drawable.power_attachment_pin
}
