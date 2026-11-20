package de.westnordost.streetcomplete.quests.oneway

import kotlinx.serialization.Serializable

@Serializable
enum class OnewayAnswer(val osmValue: String) {
    FORWARD("yes"),
    BACKWARD("-1"),
    NO_ONEWAY("no")
}
