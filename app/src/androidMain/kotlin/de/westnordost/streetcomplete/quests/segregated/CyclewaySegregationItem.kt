package de.westnordost.streetcomplete.quests.segregated

import de.westnordost.streetcomplete.quests.segregated.CyclewaySegregation.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_segregated_mixed
import de.westnordost.streetcomplete.resources.quest_segregated_separated
import de.westnordost.streetcomplete.resources.separate_cycleway_not_segregated
import de.westnordost.streetcomplete.resources.separate_cycleway_segregated
import de.westnordost.streetcomplete.resources.separate_cycleway_segregated_l
import de.westnordost.streetcomplete.resources.separate_cycleway_with_sidewalk
import de.westnordost.streetcomplete.resources.separate_cycleway_with_sidewalk_l
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val CyclewaySegregation.title: StringResource get() = when (this) {
    YES -> Res.string.quest_segregated_separated
    NO -> Res.string.quest_segregated_mixed
    SIDEWALK -> Res.string.separate_cycleway_with_sidewalk
}

fun CyclewaySegregation.getIcon(isLeftHandTraffic: Boolean): DrawableResource = when (this) {
    YES ->
        if (isLeftHandTraffic) {
            Res.drawable.separate_cycleway_segregated_l
        } else {
            Res.drawable.separate_cycleway_segregated
        }
    NO ->
        Res.drawable.separate_cycleway_not_segregated
    SIDEWALK ->
        if (isLeftHandTraffic) {
            Res.drawable.separate_cycleway_with_sidewalk_l
        } else {
            Res.drawable.separate_cycleway_with_sidewalk
        }
}
