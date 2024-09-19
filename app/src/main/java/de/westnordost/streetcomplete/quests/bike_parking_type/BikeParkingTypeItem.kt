package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.*
import de.westnordost.streetcomplete.view.image_select.Item

fun BikeParkingType.asItem() = Item(this, iconResId, titleResId)

private val BikeParkingType.titleResId: Int get() = when (this) {
    STANDS ->           R.string.quest_bicycle_parking_type_stand
    WALL_LOOPS ->       R.string.quest_bicycle_parking_type_wheelbender
    SAFE_LOOPS ->       R.string.quest_bicycle_parking_type_safeloops
    SHED ->             R.string.quest_bicycle_parking_type_shed
    LOCKERS ->          R.string.quest_bicycle_parking_type_locker
    BUILDING ->         R.string.quest_bicycle_parking_type_building
    HANDLEBAR_HOLDER -> R.string.quest_bicycle_parking_type_handlebarholder
    SADDLE_HOLDER ->    R.string.quest_bicycle_parking_type_saddleholder
    TWO_TIER ->         R.string.quest_bicycle_parking_type_two_tier
    FLOOR ->            R.string.quest_bicycle_parking_type_floor
}

private val BikeParkingType.iconResId: Int get() = when (this) {
    STANDS ->           R.drawable.bicycle_parking_type_stand
    WALL_LOOPS ->       R.drawable.bicycle_parking_type_wheelbenders
    SAFE_LOOPS ->       R.drawable.bicycle_parking_type_safeloops
    SHED ->             R.drawable.bicycle_parking_type_shed
    LOCKERS ->          R.drawable.bicycle_parking_type_lockers
    BUILDING ->         R.drawable.bicycle_parking_type_building
    HANDLEBAR_HOLDER -> R.drawable.bicycle_parking_type_handlebarholder
    SADDLE_HOLDER ->    R.drawable.bicycle_parking_type_saddleholder
    TWO_TIER ->         R.drawable.bicycle_parking_type_two_tier
    FLOOR ->            R.drawable.bicycle_parking_type_floor
}
