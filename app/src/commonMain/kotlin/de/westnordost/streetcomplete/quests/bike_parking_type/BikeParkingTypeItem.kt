package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.quests.bike_parking_type.BikeParkingType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.bicycle_parking_type_building
import de.westnordost.streetcomplete.resources.bicycle_parking_type_floor
import de.westnordost.streetcomplete.resources.bicycle_parking_type_handlebarholder
import de.westnordost.streetcomplete.resources.bicycle_parking_type_lockers
import de.westnordost.streetcomplete.resources.bicycle_parking_type_safeloops
import de.westnordost.streetcomplete.resources.bicycle_parking_type_shed
import de.westnordost.streetcomplete.resources.bicycle_parking_type_stand
import de.westnordost.streetcomplete.resources.bicycle_parking_type_two_tier
import de.westnordost.streetcomplete.resources.bicycle_parking_type_wheelbenders
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_building
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_floor
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_handlebarholder
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_locker
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_safeloops
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_shed
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_stand
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_two_tier
import de.westnordost.streetcomplete.resources.quest_bicycle_parking_type_wheelbender
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BikeParkingType.title: StringResource get() = when (this) {
    STANDS ->           Res.string.quest_bicycle_parking_type_stand
    WALL_LOOPS ->       Res.string.quest_bicycle_parking_type_wheelbender
    SAFE_LOOPS ->       Res.string.quest_bicycle_parking_type_safeloops
    SHED ->             Res.string.quest_bicycle_parking_type_shed
    LOCKERS ->          Res.string.quest_bicycle_parking_type_locker
    BUILDING ->         Res.string.quest_bicycle_parking_type_building
    HANDLEBAR_HOLDER -> Res.string.quest_bicycle_parking_type_handlebarholder
    TWO_TIER ->         Res.string.quest_bicycle_parking_type_two_tier
    FLOOR ->            Res.string.quest_bicycle_parking_type_floor
}

val BikeParkingType.icon: DrawableResource get() = when (this) {
    STANDS ->           Res.drawable.bicycle_parking_type_stand
    WALL_LOOPS ->       Res.drawable.bicycle_parking_type_wheelbenders
    SAFE_LOOPS ->       Res.drawable.bicycle_parking_type_safeloops
    SHED ->             Res.drawable.bicycle_parking_type_shed
    LOCKERS ->          Res.drawable.bicycle_parking_type_lockers
    BUILDING ->         Res.drawable.bicycle_parking_type_building
    HANDLEBAR_HOLDER -> Res.drawable.bicycle_parking_type_handlebarholder
    TWO_TIER ->         Res.drawable.bicycle_parking_type_two_tier
    FLOOR ->            Res.drawable.bicycle_parking_type_floor
}
