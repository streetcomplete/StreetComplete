package de.westnordost.streetcomplete.quests.segregated

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item

fun CyclewaySegregation.asItem(isLeftHandTraffic: Boolean) =
    Item(this, getIconResId(isLeftHandTraffic), titleResId)

private val CyclewaySegregation.titleResId: Int get() = when (this.value) {
    true -> R.string.quest_segregated_separated
    false -> R.string.quest_segregated_mixed
}

private fun CyclewaySegregation.getIconResId(isLeftHandTraffic: Boolean): Int = when (this.value) {
    true -> if (isLeftHandTraffic) R.drawable.ic_path_segregated_l else R.drawable.ic_path_segregated
    false -> R.drawable.ic_path_segregated_no
}
