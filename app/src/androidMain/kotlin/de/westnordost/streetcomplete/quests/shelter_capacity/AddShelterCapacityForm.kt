package de.westnordost.streetcomplete.quests.shelter_capacity

import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.count_electric_car

class AddShelterCapacityForm : AAddCountInput() {
    override val icon = Res.drawable.count_electric_car
    override val initialCount get() = element.tags["capacity"]?.toIntOrNull()
}
