package de.westnordost.streetcomplete.quests.motorcycle_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AAddCountInput

class AddMotorcycleParkingCapacityForm : AAddCountInput() {
    override val iconId = R.drawable.ic_motorcycle
    override val initialCount get() = element.tags["capacity"]?.toIntOrNull()
}
