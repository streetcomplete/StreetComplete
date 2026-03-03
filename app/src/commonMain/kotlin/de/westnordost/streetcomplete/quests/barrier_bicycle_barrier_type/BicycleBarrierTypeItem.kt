package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.barrier_bicycle_barrier_diagonal
import de.westnordost.streetcomplete.resources.barrier_bicycle_barrier_double
import de.westnordost.streetcomplete.resources.barrier_bicycle_barrier_single
import de.westnordost.streetcomplete.resources.barrier_bicycle_barrier_tilted
import de.westnordost.streetcomplete.resources.barrier_bicycle_barrier_triple
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_type_diagonal
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_type_double
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_type_multiple
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_type_single
import de.westnordost.streetcomplete.resources.quest_barrier_bicycle_type_tilted
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BicycleBarrierType.title: StringResource get() = when (this) {
    SINGLE ->   Res.string.quest_barrier_bicycle_type_single
    DOUBLE ->   Res.string.quest_barrier_bicycle_type_double
    TRIPLE ->   Res.string.quest_barrier_bicycle_type_multiple
    DIAGONAL -> Res.string.quest_barrier_bicycle_type_diagonal
    TILTED ->   Res.string.quest_barrier_bicycle_type_tilted
}

val BicycleBarrierType.icon: DrawableResource get() = when (this) {
    SINGLE ->   Res.drawable.barrier_bicycle_barrier_single
    DOUBLE ->   Res.drawable.barrier_bicycle_barrier_double
    TRIPLE ->   Res.drawable.barrier_bicycle_barrier_triple
    DIAGONAL -> Res.drawable.barrier_bicycle_barrier_diagonal
    TILTED ->   Res.drawable.barrier_bicycle_barrier_tilted
}
