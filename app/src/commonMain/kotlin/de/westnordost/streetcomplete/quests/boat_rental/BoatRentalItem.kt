package de.westnordost.streetcomplete.quests.boat_rental

import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.*
import de.westnordost.streetcomplete.resources.*
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
