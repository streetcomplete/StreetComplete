package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.quests.railway_crossing.RailwayCrossingBarrier.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_railway_crossing_barrier_none2
import de.westnordost.streetcomplete.resources.railway_crossing_chicane
import de.westnordost.streetcomplete.resources.railway_crossing_double_half
import de.westnordost.streetcomplete.resources.railway_crossing_full
import de.westnordost.streetcomplete.resources.railway_crossing_full_l
import de.westnordost.streetcomplete.resources.railway_crossing_gate
import de.westnordost.streetcomplete.resources.railway_crossing_half
import de.westnordost.streetcomplete.resources.railway_crossing_half_l
import de.westnordost.streetcomplete.resources.railway_crossing_none
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
