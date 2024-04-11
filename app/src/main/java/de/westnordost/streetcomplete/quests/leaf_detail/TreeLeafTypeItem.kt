package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.leaf_detail.TreeLeafType.BROADLEAVED
import de.westnordost.streetcomplete.quests.leaf_detail.TreeLeafType.NEEDLELEAVED
import de.westnordost.streetcomplete.view.image_select.Item

fun TreeLeafType.asItem() = Item(this, iconResId, titleResId)

private val TreeLeafType.titleResId: Int get() = when (this) {
    NEEDLELEAVED -> R.string.quest_leaf_type_needles
    BROADLEAVED ->  R.string.quest_leaf_type_broadleaved
}

private val TreeLeafType.iconResId: Int get() = when (this) {
    NEEDLELEAVED -> R.drawable.leaf_type_needleleaved
    BROADLEAVED ->  R.drawable.leaf_type_broadleaved
}
