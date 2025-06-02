package de.westnordost.streetcomplete.quests.charging_station_capacity

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AAddCountInput

class AddChargingStationCapacityForm : AAddCountInput() {
    override val iconId = R.drawable.ic_electric_car
    override val initialCount get() = element.tags["capacity"]?.toIntOrNull()
}
