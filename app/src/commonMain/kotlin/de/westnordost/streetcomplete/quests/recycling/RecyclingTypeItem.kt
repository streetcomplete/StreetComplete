package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.quests.recycling.RecyclingType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.overground_recycling_container
import de.westnordost.streetcomplete.resources.recycling_centre
import de.westnordost.streetcomplete.resources.recycling_type_centre
import de.westnordost.streetcomplete.resources.recycling_type_container
import de.westnordost.streetcomplete.resources.recycling_type_container_underground
import de.westnordost.streetcomplete.resources.underground_recycling_container
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val RecyclingType.title: StringResource get() = when (this) {
    OVERGROUND_CONTAINER ->  Res.string.overground_recycling_container
    UNDERGROUND_CONTAINER -> Res.string.underground_recycling_container
    RECYCLING_CENTRE ->      Res.string.recycling_centre
}

val RecyclingType.icon: DrawableResource get() = when (this) {
    OVERGROUND_CONTAINER ->  Res.drawable.recycling_type_container
    UNDERGROUND_CONTAINER -> Res.drawable.recycling_type_container_underground
    RECYCLING_CENTRE ->      Res.drawable.recycling_type_centre
}
