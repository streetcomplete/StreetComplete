package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FIXED
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FLEXIBLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.FOLDABLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.REMOVABLE
import de.westnordost.streetcomplete.quests.bollard_type.BollardType.RISING
import de.westnordost.streetcomplete.view.image_select.Item

fun BollardType.asItem() = Item(this, iconResId, titleResId)

private val BollardType.titleResId: Int get() = when (this) {
    RISING ->    R.string.quest_bollard_type_rising
    REMOVABLE -> R.string.quest_bollard_type_removable
    FOLDABLE ->  R.string.quest_bollard_type_foldable2
    FLEXIBLE ->  R.string.quest_bollard_type_flexible
    FIXED ->     R.string.quest_bollard_type_fixed
}

private val BollardType.iconResId: Int get() = when (this) {
    RISING ->    R.drawable.bollard_rising
    REMOVABLE -> R.drawable.bollard_removable
    FOLDABLE ->  R.drawable.bollard_foldable
    FLEXIBLE ->  R.drawable.bollard_flexible
    FIXED ->     R.drawable.bollard_fixed
}
