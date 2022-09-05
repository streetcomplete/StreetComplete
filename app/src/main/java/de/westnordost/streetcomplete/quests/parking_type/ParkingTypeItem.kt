package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.LANE
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.MULTI_STOREY
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.STREET_SIDE
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.SURFACE
import de.westnordost.streetcomplete.quests.parking_type.ParkingType.UNDERGROUND
import de.westnordost.streetcomplete.view.image_select.Item

fun ParkingType.asItem() = Item(this, iconResId, titleResId)

private val ParkingType.titleResId: Int get() = when (this) {
    SURFACE ->      R.string.quest_parkingType_surface
    STREET_SIDE ->  R.string.quest_parkingType_street_side
    LANE ->         R.string.quest_parkingType_lane
    UNDERGROUND ->  R.string.quest_parkingType_underground
    MULTI_STOREY -> R.string.quest_parkingType_multiStorage
}

private val ParkingType.iconResId: Int get() = when (this) {
    SURFACE ->      R.drawable.parking_type_surface
    STREET_SIDE ->  R.drawable.parking_type_street_side
    LANE ->         R.drawable.parking_type_lane
    UNDERGROUND ->  R.drawable.parking_type_underground
    MULTI_STOREY -> R.drawable.parking_type_multistorey
}
