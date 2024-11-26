package de.westnordost.streetcomplete.quests.boat_rental

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.MOTORBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.HOUSEBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.PEDALBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.JETSKI
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.SAILBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.ROWBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.KAYAK
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.CANOE
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.SUP
import de.westnordost.streetcomplete.view.image_select.Item

fun BoatRental.asItem() = Item(this, iconResId, titleResId)

private val BoatRental.titleResId: Int get() = when (this) {
    CANOE ->     R.string.quest_boat_rental_canoe
    KAYAK ->     R.string.quest_boat_rental_kayak
    PEDALBOAT -> R.string.quest_boat_rental_pedalboat
    MOTORBOAT -> R.string.quest_boat_rental_motorboat
    SUP ->       R.string.quest_boat_rental_standup_paddleboard
    SAILBOAT ->  R.string.quest_boat_rental_sailboat
    JETSKI ->    R.string.quest_boat_rental_jetski
    HOUSEBOAT -> R.string.quest_boat_rental_houseboat
    ROWBOAT ->   R.string.quest_boat_rental_rowboat
}
private val BoatRental.iconResId: Int get() = when (this) {
    CANOE ->     R.drawable.ic_boat_rental_canoe
    KAYAK ->     R.drawable.ic_boat_rental_kayak
    PEDALBOAT -> R.drawable.ic_boat_rental_pedalboat
    MOTORBOAT -> R.drawable.ic_boat_rental_motorboat
    SUP ->       R.drawable.ic_boat_rental_standup_paddleboard
    SAILBOAT ->  R.drawable.ic_boat_rental_sailboat
    JETSKI ->    R.drawable.ic_boat_rental_jetski
    HOUSEBOAT -> R.drawable.ic_boat_rental_houseboat
    ROWBOAT ->   R.drawable.ic_boat_rental_rowboat
}
