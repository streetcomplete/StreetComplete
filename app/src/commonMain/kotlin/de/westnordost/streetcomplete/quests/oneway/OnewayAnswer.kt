package de.westnordost.streetcomplete.quests.oneway

import kotlinx.serialization.Serializable

@Serializable
enum class OnewayAnswer {
    FORWARD,
    BACKWARD,
    NO_ONEWAY
}
