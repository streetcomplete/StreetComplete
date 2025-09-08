package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation.BicycleBarrierInstallation.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.barrier_bicycle_installation_fixed
import de.westnordost.streetcomplete.resources.barrier_bicycle_installation_openable
import de.westnordost.streetcomplete.resources.barrier_bicycle_installation_removable
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_installation_fixed
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_installation_openable
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_installation_removable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BicycleBarrierInstallation.title: StringResource get() = when (this) {
    FIXED ->     Res.string.quest_barrier_bicycle_installation_fixed
    OPENABLE ->  Res.string.quest_barrier_bicycle_installation_openable
    REMOVABLE -> Res.string.quest_barrier_bicycle_installation_removable
}

val BicycleBarrierInstallation.icon: DrawableResource get() = when (this) {
    FIXED ->     Res.drawable.barrier_bicycle_installation_fixed
    OPENABLE ->  Res.drawable.barrier_bicycle_installation_openable
    REMOVABLE -> Res.drawable.barrier_bicycle_installation_removable
}
