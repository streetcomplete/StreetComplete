package de.westnordost.streetcomplete.quests.parking_type

import de.westnordost.streetcomplete.quests.parking_type.ParkingType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.parking_type_lane
import de.westnordost.streetcomplete.resources.parking_type_multistorey
import de.westnordost.streetcomplete.resources.parking_type_street_side
import de.westnordost.streetcomplete.resources.parking_type_surface
import de.westnordost.streetcomplete.resources.parking_type_underground
import de.westnordost.streetcomplete.resources.quest_parkingType_lane
import de.westnordost.streetcomplete.resources.quest_parkingType_multiStorage
import de.westnordost.streetcomplete.resources.quest_parkingType_street_side
import de.westnordost.streetcomplete.resources.quest_parkingType_surface
import de.westnordost.streetcomplete.resources.quest_parkingType_underground
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val ParkingType.title: StringResource get() = when (this) {
    SURFACE ->      Res.string.quest_parkingType_surface
    STREET_SIDE ->  Res.string.quest_parkingType_street_side
    LANE ->         Res.string.quest_parkingType_lane
    UNDERGROUND ->  Res.string.quest_parkingType_underground
    MULTI_STOREY -> Res.string.quest_parkingType_multiStorage
}

val ParkingType.icon: DrawableResource get() = when (this) {
    SURFACE ->      Res.drawable.parking_type_surface
    STREET_SIDE ->  Res.drawable.parking_type_street_side
    LANE ->         Res.drawable.parking_type_lane
    UNDERGROUND ->  Res.drawable.parking_type_underground
    MULTI_STOREY -> Res.drawable.parking_type_multistorey
}
