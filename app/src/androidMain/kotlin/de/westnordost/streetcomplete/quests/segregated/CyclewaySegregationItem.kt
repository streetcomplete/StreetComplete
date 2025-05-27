package de.westnordost.streetcomplete.quests.segregated

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.segregated.CyclewaySegregation.*
import de.westnordost.streetcomplete.view.image_select.Item

fun CyclewaySegregation.asItem(isLeftHandTraffic: Boolean) =
    Item(this, getIconResId(isLeftHandTraffic), titleResId)

private val CyclewaySegregation.titleResId: Int get() = when (this) {
    YES -> R.string.quest_segregated_separated
    NO -> R.string.quest_segregated_mixed
    SIDEWALK -> R.string.separate_cycleway_with_sidewalk
}

private fun CyclewaySegregation.getIconResId(isLeftHandTraffic: Boolean): Int = when (this) {
    YES ->
        if (isLeftHandTraffic) {
            R.drawable.ic_separate_cycleway_segregated_l
        } else {
            R.drawable.ic_separate_cycleway_segregated
        }
    NO ->
        R.drawable.ic_separate_cycleway_not_segregated
    SIDEWALK ->
        if (isLeftHandTraffic) {
            R.drawable.ic_separate_cycleway_with_sidewalk_l
        } else {
            R.drawable.ic_separate_cycleway_with_sidewalk
        }
}
