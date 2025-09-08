package de.westnordost.streetcomplete.quests.boat_rental

import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.boat_rental_canoe
import de.westnordost.streetcomplete.resources.boat_rental_houseboat
import de.westnordost.streetcomplete.resources.boat_rental_jetski
import de.westnordost.streetcomplete.resources.boat_rental_kayak
import de.westnordost.streetcomplete.resources.boat_rental_motorboat
import de.westnordost.streetcomplete.resources.boat_rental_pedalboat
import de.westnordost.streetcomplete.resources.boat_rental_raft
import de.westnordost.streetcomplete.resources.boat_rental_rowboat
import de.westnordost.streetcomplete.resources.boat_rental_sailboard
import de.westnordost.streetcomplete.resources.boat_rental_sailboat
import de.westnordost.streetcomplete.resources.boat_rental_standup_paddleboard
import de.westnordost.streetcomplete.resources.boat_rental_surfboard
import de.westnordost.streetcomplete.resources.boat_rental_yacht
import de.westnordost.streetcomplete.resources.quest_boat_rental_canoe
import de.westnordost.streetcomplete.resources.quest_boat_rental_houseboat
import de.westnordost.streetcomplete.resources.quest_boat_rental_jetski
import de.westnordost.streetcomplete.resources.quest_boat_rental_kayak
import de.westnordost.streetcomplete.resources.quest_boat_rental_motorboat
import de.westnordost.streetcomplete.resources.quest_boat_rental_pedalboat
import de.westnordost.streetcomplete.resources.quest_boat_rental_raft
import de.westnordost.streetcomplete.resources.quest_boat_rental_rowboat
import de.westnordost.streetcomplete.resources.quest_boat_rental_sailboard
import de.westnordost.streetcomplete.resources.quest_boat_rental_sailboat
import de.westnordost.streetcomplete.resources.quest_boat_rental_standup_paddleboard
import de.westnordost.streetcomplete.resources.quest_boat_rental_surfboard
import de.westnordost.streetcomplete.resources.quest_boat_rental_yacht
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BoatRental.title: StringResource get() = when (this) {
    CANOE ->     Res.string.quest_boat_rental_canoe
    KAYAK ->     Res.string.quest_boat_rental_kayak
    PEDALBOAT -> Res.string.quest_boat_rental_pedalboat
    SUP ->       Res.string.quest_boat_rental_standup_paddleboard
    ROWBOAT ->   Res.string.quest_boat_rental_rowboat
    SAILBOAT ->  Res.string.quest_boat_rental_sailboat
    RAFT ->      Res.string.quest_boat_rental_raft
    SURFBOARD -> Res.string.quest_boat_rental_surfboard
    SAILBOARD -> Res.string.quest_boat_rental_sailboard
    MOTORBOAT -> Res.string.quest_boat_rental_motorboat
    JETSKI ->    Res.string.quest_boat_rental_jetski
    HOUSEBOAT -> Res.string.quest_boat_rental_houseboat
    YACHT ->     Res.string.quest_boat_rental_yacht
}

val BoatRental.icon: DrawableResource get() = when (this) {
    CANOE ->     Res.drawable.boat_rental_canoe
    KAYAK ->     Res.drawable.boat_rental_kayak
    PEDALBOAT -> Res.drawable.boat_rental_pedalboat
    SUP ->       Res.drawable.boat_rental_standup_paddleboard
    ROWBOAT ->   Res.drawable.boat_rental_rowboat
    SAILBOAT ->  Res.drawable.boat_rental_sailboat
    RAFT ->      Res.drawable.boat_rental_raft
    SURFBOARD -> Res.drawable.boat_rental_surfboard
    SAILBOARD -> Res.drawable.boat_rental_sailboard
    MOTORBOAT -> Res.drawable.boat_rental_motorboat
    JETSKI ->    Res.drawable.boat_rental_jetski
    HOUSEBOAT -> Res.drawable.boat_rental_houseboat
    YACHT ->     Res.drawable.boat_rental_yacht
}
