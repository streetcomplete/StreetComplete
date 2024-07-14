package de.westnordost.streetcomplete.quests.boat_rental

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.MOTORBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.HOUSEBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.PEDALBOARD
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.JETSKI
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.SAILBOAT
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.DINGHY
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.KAYAK
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.CANOE
import de.westnordost.streetcomplete.quests.boat_rental.BoatRental.PADDLEBOARD
import de.westnordost.streetcomplete.view.image_select.Item

fun BoatRental.asItem() = Item(this, iconResId, titleResId)

private val BoatRental.titleResId: Int get() = when (this) {
    CANOE ->       R.string.quest_boat_rental_canoe
    KAYAK ->       R.string.quest_boat_rental_kayak
    PEDALBOARD ->  R.string.quest_boat_rental_pedalboard
    MOTORBOAT ->   R.string.quest_boat_rental_motorboat
    PADDLEBOARD -> R.string.quest_boat_rental_paddleboard
    SAILBOAT ->    R.string.quest_boat_rental_sailboat
    JETSKI ->      R.string.quest_boat_rental_jetski
    HOUSEBOAT ->   R.string.quest_boat_rental_houseboat
    DINGHY ->      R.string.quest_boat_rental_dinghy
}
private val BoatRental.iconResId: Int get() = when (this) {
    CANOE ->       R.drawable.ic_boat_rental_canoe
    KAYAK ->       R.drawable.ic_boat_rental_kayak
    PEDALBOARD ->  R.drawable.ic_boat_rental_pedalboard
    MOTORBOAT ->   R.drawable.ic_boat_rental_motorboat
    PADDLEBOARD -> R.drawable.ic_boat_rental_standup_paddleboard
    SAILBOAT ->    R.drawable.ic_boat_rental_sailboat
    JETSKI ->      R.drawable.ic_boat_rental_jetski
    HOUSEBOAT ->   R.drawable.ic_boat_rental_houseboat
    DINGHY ->      R.drawable.ic_boat_rental_dinghy
}
