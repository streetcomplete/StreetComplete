package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation.BicycleBarrierInstallation.FIXED
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation.BicycleBarrierInstallation.OPENABLE
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation.BicycleBarrierInstallation.REMOVABLE
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation.BicycleBarrierInstallation.FOLDABLE
import de.westnordost.streetcomplete.view.image_select.Item

fun BicycleBarrierInstallation.asItem() = Item(this, iconResId, titleResId)

private val BicycleBarrierInstallation.titleResId: Int get() = when (this) {
    FIXED ->     R.string.quest_barrier_bicycle_installation_fixed
    OPENABLE ->  R.string.quest_barrier_bicycle_installation_openable
    REMOVABLE -> R.string.quest_barrier_bicycle_installation_removable
    FOLDABLE ->  R.string.quest_barrier_bicycle_installation_foldable
}

private val BicycleBarrierInstallation.iconResId: Int get() = when (this) {
    FIXED ->     R.drawable.barrier_bicycle_installation_fixed
    OPENABLE ->  R.drawable.barrier_bicycle_installation_openable
    REMOVABLE -> R.drawable.barrier_bicycle_installation_removable
    FOLDABLE ->  R.drawable.barrier_bicycle_installation_foldable
}
