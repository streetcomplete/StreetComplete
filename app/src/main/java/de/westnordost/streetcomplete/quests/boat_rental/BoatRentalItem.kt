package de.westnordost.streetcomplete.quests.boat_rental

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.*
import de.westnordost.streetcomplete.view.image_select.Item

fun BoatRental.asItem() = Item(this, iconResId, titleResId)

private val BoatRental.titleResId: Int get() = when (this) {
    CANOE ->     R.string.quest_boat_rental_canoe
    KAYAK ->     R.string.quest_boat_rental_kayak
    PEDALBOAT -> R.string.quest_boat_rental_pedalboat
    SUP ->       R.string.quest_boat_rental_standup_paddleboard
    ROWBOAT ->   R.string.quest_boat_rental_rowboat
    SAILBOAT ->  R.string.quest_boat_rental_sailboat
    RAFT ->      R.string.quest_boat_rental_raft
    SURFBOARD -> R.string.quest_boat_rental_surfboard
    SAILBOARD -> R.string.quest_boat_rental_sailboard
    MOTORBOAT -> R.string.quest_boat_rental_motorboat
    JETSKI ->    R.string.quest_boat_rental_jetski
    HOUSEBOAT -> R.string.quest_boat_rental_houseboat
    YACHT ->     R.string.quest_boat_rental_yacht
}
private val BoatRental.iconResId: Int get() = when (this) {
    CANOE ->     R.drawable.ic_boat_rental_canoe
    KAYAK ->     R.drawable.ic_boat_rental_kayak
    PEDALBOAT -> R.drawable.ic_boat_rental_pedalboat
    SUP ->       R.drawable.ic_boat_rental_standup_paddleboard
    ROWBOAT ->   R.drawable.ic_boat_rental_rowboat
    SAILBOAT ->  R.drawable.ic_boat_rental_sailboat
    RAFT ->      R.drawable.ic_boat_rental_raft
    SURFBOARD -> R.drawable.ic_boat_rental_surfboard
    SAILBOARD -> R.drawable.ic_boat_rental_sailboard
    MOTORBOAT -> R.drawable.ic_boat_rental_motorboat
    JETSKI ->    R.drawable.ic_boat_rental_jetski
    HOUSEBOAT -> R.drawable.ic_boat_rental_houseboat
    YACHT ->     R.drawable.ic_boat_rental_yacht
}
