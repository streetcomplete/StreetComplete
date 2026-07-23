package de.westnordost.streetcomplete.quests.power_attachment

import kotlinx.serialization.Serializable

@Serializable
enum class PowerAttachment(val osmValue: String) {
    SUSPENSION("suspension"),
    ANCHOR("anchor"),
    PIN("pin"),
}
