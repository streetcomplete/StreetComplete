package de.westnordost.streetcomplete.quests.fire_hydrant

import kotlinx.serialization.Serializable

@Serializable
enum class FireHydrantType(val osmValue: String) {
    PILLAR("pillar"),
    UNDERGROUND("underground"),
    WALL("wall"),
    PIPE("pipe"),
}
