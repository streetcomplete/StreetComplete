package de.westnordost.streetcomplete.quests.charging_station_capacity

import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.count_bicycle

class AddChargingStationBicycleCapacityForm : AAddCountInput() {
    override val icon = Res.drawable.count_bicycle
}
