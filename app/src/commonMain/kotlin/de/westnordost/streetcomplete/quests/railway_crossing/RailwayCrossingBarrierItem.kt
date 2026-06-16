package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val RailwayCrossingBarrier.title: StringResource? get() = when (this) {
    NO ->   Res.string.quest_railway_crossing_barrier_none2
    else -> null
}

fun RailwayCrossingBarrier.getIcon(isLeftHandTraffic: Boolean): DrawableResource = when (this) {
    NO ->          Res.drawable.railway_crossing_none
    HALF ->        if (isLeftHandTraffic) Res.drawable.railway_crossing_half_l else Res.drawable.railway_crossing_half
    DOUBLE_HALF -> Res.drawable.railway_crossing_double_half
    FULL ->        if (isLeftHandTraffic) Res.drawable.railway_crossing_full_l else Res.drawable.railway_crossing_full
    GATE ->        Res.drawable.railway_crossing_gate
    CHICANE ->     Res.drawable.railway_crossing_chicane
}
