package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.view.image_select.Item

fun SeparateCycleway.asItem(isLeftHandTraffic: Boolean) =
    Item(this, getIconResId(isLeftHandTraffic), titleResId)

private val SeparateCycleway.titleResId: Int get() = when (this) {
    PATH ->               R.string.separate_cycleway_path
    NOT_ALLOWED ->        R.string.separate_cycleway_no_signed
    ALLOWED_ON_FOOTWAY -> R.string.separate_cycleway_footway_allowed_sign
    NON_DESIGNATED_ON_FOOTWAY -> R.string.separate_cycleway_no_or_allowed
    NON_SEGREGATED ->     R.string.separate_cycleway_non_segregated
    SEGREGATED ->         R.string.separate_cycleway_segregated
    EXCLUSIVE ->          R.string.separate_cycleway_exclusive
    EXCLUSIVE_WITH_SIDEWALK -> R.string.separate_cycleway_with_sidewalk
}

private fun SeparateCycleway.getIconResId(isLeftHandTraffic: Boolean): Int = when (this) {
    PATH ->               R.drawable.ic_separate_cycleway_path
    NOT_ALLOWED ->        R.drawable.ic_separate_cycleway_disallowed
    ALLOWED_ON_FOOTWAY -> R.drawable.ic_separate_cycleway_allowed
    NON_DESIGNATED_ON_FOOTWAY -> R.drawable.ic_separate_cycleway_no
    NON_SEGREGATED ->     R.drawable.ic_separate_cycleway_not_segregated
    SEGREGATED ->
        if (isLeftHandTraffic) {
            R.drawable.ic_separate_cycleway_segregated_l
        } else {
            R.drawable.ic_separate_cycleway_segregated
        }
    EXCLUSIVE ->          R.drawable.ic_separate_cycleway_exclusive
    EXCLUSIVE_WITH_SIDEWALK ->
        if (isLeftHandTraffic) {
            R.drawable.ic_separate_cycleway_with_sidewalk_l
        } else {
            R.drawable.ic_separate_cycleway_with_sidewalk
        }
}
