package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.DIAGONAL
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.DOUBLE
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.SINGLE
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.TILTED
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.TRIPLE
import de.westnordost.streetcomplete.view.image_select.Item

fun BicycleBarrierType.asItem() = Item(this, iconResId, titleResId)

private val BicycleBarrierType.titleResId: Int get() = when (this) {
    SINGLE ->   R.string.quest_barrier_bicycle_type_single
    DOUBLE ->   R.string.quest_barrier_bicycle_type_double
    TRIPLE ->   R.string.quest_barrier_bicycle_type_multiple
    DIAGONAL -> R.string.quest_barrier_bicycle_type_diagonal
    TILTED ->   R.string.quest_barrier_bicycle_type_tilted
}

private val BicycleBarrierType.iconResId: Int get() = when (this) {
    SINGLE ->   R.drawable.barrier_bicycle_barrier_single
    DOUBLE ->   R.drawable.barrier_bicycle_barrier_double
    TRIPLE ->   R.drawable.barrier_bicycle_barrier_triple
    DIAGONAL -> R.drawable.barrier_bicycle_barrier_diagonal
    TILTED ->   R.drawable.barrier_bicycle_barrier_tilted
}
