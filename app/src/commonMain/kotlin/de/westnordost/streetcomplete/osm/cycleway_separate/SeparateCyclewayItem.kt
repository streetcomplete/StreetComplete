package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.separate_cycleway_allowed
import de.westnordost.streetcomplete.resources.separate_cycleway_disallowed
import de.westnordost.streetcomplete.resources.separate_cycleway_exclusive
import de.westnordost.streetcomplete.resources.separate_cycleway_footway_allowed_sign
import de.westnordost.streetcomplete.resources.separate_cycleway_no
import de.westnordost.streetcomplete.resources.separate_cycleway_no_or_allowed
import de.westnordost.streetcomplete.resources.separate_cycleway_no_signed
import de.westnordost.streetcomplete.resources.separate_cycleway_non_segregated
import de.westnordost.streetcomplete.resources.separate_cycleway_not_segregated
import de.westnordost.streetcomplete.resources.separate_cycleway_path
import de.westnordost.streetcomplete.resources.separate_cycleway_segregated
import de.westnordost.streetcomplete.resources.separate_cycleway_segregated_l
import de.westnordost.streetcomplete.resources.separate_cycleway_with_sidewalk
import de.westnordost.streetcomplete.resources.separate_cycleway_with_sidewalk_l
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val SeparateCycleway.title: StringResource get() = when (this) {
    PATH ->               Res.string.separate_cycleway_path
    NOT_ALLOWED ->        Res.string.separate_cycleway_no_signed
    ALLOWED_ON_FOOTWAY -> Res.string.separate_cycleway_footway_allowed_sign
    NON_DESIGNATED_ON_FOOTWAY -> Res.string.separate_cycleway_no_or_allowed
    NON_SEGREGATED ->     Res.string.separate_cycleway_non_segregated
    SEGREGATED ->         Res.string.separate_cycleway_segregated
    EXCLUSIVE ->          Res.string.separate_cycleway_exclusive
    EXCLUSIVE_WITH_SIDEWALK -> Res.string.separate_cycleway_with_sidewalk
}

fun SeparateCycleway.getIcon(isLeftHandTraffic: Boolean): DrawableResource = when (this) {
    PATH ->               Res.drawable.separate_cycleway_path
    NOT_ALLOWED ->        Res.drawable.separate_cycleway_disallowed
    ALLOWED_ON_FOOTWAY -> Res.drawable.separate_cycleway_allowed
    NON_DESIGNATED_ON_FOOTWAY -> Res.drawable.separate_cycleway_no
    NON_SEGREGATED ->     Res.drawable.separate_cycleway_not_segregated
    SEGREGATED ->
        if (isLeftHandTraffic) {
            Res.drawable.separate_cycleway_segregated_l
        } else {
            Res.drawable.separate_cycleway_segregated
        }
    EXCLUSIVE ->          Res.drawable.separate_cycleway_exclusive
    EXCLUSIVE_WITH_SIDEWALK ->
        if (isLeftHandTraffic) {
            Res.drawable.separate_cycleway_with_sidewalk_l
        } else {
            Res.drawable.separate_cycleway_with_sidewalk
        }
}
