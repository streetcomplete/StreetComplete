package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.BUILDING
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.HANDLEBAR_HOLDER
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.LOCKERS
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.SHED
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.STANDS
import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.WALL_LOOPS
import de.westnordost.streetcomplete.view.image_select.Item

fun BikeParkingType.asItem() = Item(this, iconResId, titleResId)

private val BikeParkingType.titleResId: Int get() = when (this) {
    STANDS ->           R.string.quest_bicycle_parking_type_stand
    WALL_LOOPS ->       R.string.quest_bicycle_parking_type_wheelbender
    SHED ->             R.string.quest_bicycle_parking_type_shed
    LOCKERS ->          R.string.quest_bicycle_parking_type_locker
    BUILDING ->         R.string.quest_bicycle_parking_type_building
    HANDLEBAR_HOLDER -> R.string.quest_bicycle_parking_type_handlebarholder
}

private val BikeParkingType.iconResId: Int get() = when (this) {
    STANDS ->           R.drawable.bicycle_parking_type_stand
    WALL_LOOPS ->       R.drawable.bicycle_parking_type_wheelbenders
    SHED ->             R.drawable.bicycle_parking_type_shed
    LOCKERS ->          R.drawable.bicycle_parking_type_lockers
    BUILDING ->         R.drawable.bicycle_parking_type_building
    HANDLEBAR_HOLDER -> R.drawable.bicycle_parking_type_handlebarholder
}
