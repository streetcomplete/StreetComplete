package de.westnordost.streetcomplete.quests.bike_parking_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AAddCountInput

class AddBikeParkingCapacityForm : AAddCountInput() {
    override val iconId = R.drawable.ic_bicycle
    override val initialCount get() = element.tags["capacity"]?.toIntOrNull()
}
