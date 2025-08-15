package de.westnordost.streetcomplete.quests.shelter_capacity

import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.sleeping_bag

class AddShelterCapacityForm : AAddCountInput() {
    override val icon = Res.drawable.sleeping_bag
    override val initialCount get() = element.tags["capacity"]?.toIntOrNull()
}
