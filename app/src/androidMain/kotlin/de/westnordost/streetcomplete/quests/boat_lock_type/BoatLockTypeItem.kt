package de.westnordost.streetcomplete.quests.boat_lock_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.boat_lock_type.BoatLockType.AUTOMATED
import de.westnordost.streetcomplete.quests.boat_lock_type.BoatLockType.SELF_SERVICE
import de.westnordost.streetcomplete.quests.boat_lock_type.BoatLockType.MANUAL
import de.westnordost.streetcomplete.view.image_select.Item

fun BoatLockType.asItem() = Item(this, iconResId, titleResId)

private val BoatLockType.titleResId: Int get() = when (this) {
    AUTOMATED ->    R.string.quest_boat_lock_type_automated
    SELF_SERVICE -> R.string.quest_boat_lock_type_self_service
    MANUAL ->      R.string.quest_boat_lock_type_manual
}

private val BoatLockType.iconResId: Int get() = when (this) {
    AUTOMATED ->    R.drawable.ic_automated
    SELF_SERVICE -> R.drawable.ic_self_service
    MANUAL ->      R.drawable.ic_manual
}
