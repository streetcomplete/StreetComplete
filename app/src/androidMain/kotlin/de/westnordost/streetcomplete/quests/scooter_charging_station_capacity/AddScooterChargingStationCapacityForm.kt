package de.westnordost.streetcomplete.quests.scooter_charging_station_capacity

import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.count_bicycle

class AddScooterChargingStationCapacityForm : AAddCountInput() {
    override val icon = Res.drawable.count_bicycle // TODO add a scooter icon (res/drawable/ic_smoothness_scooter.xml) could work but it needs to be copied in commomMain, right know it can be found in androidMain.
}
