package de.westnordost.streetcomplete.quests.recycling

import kotlinx.serialization.Serializable

@Serializable
enum class RecyclingType {
    OVERGROUND_CONTAINER,
    UNDERGROUND_CONTAINER,
    RECYCLING_CENTRE
}
