package de.westnordost.streetcomplete.quests.bike_parking_capacity

import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.count_bicycle

class AddBikeParkingCapacityForm : AAddCountInput() {
    override val icon = Res.drawable.count_bicycle
}
