package de.westnordost.streetcomplete.quests.step_count

import de.westnordost.streetcomplete.quests.AAddCountInput
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.count_step

class AddStepCountForm : AAddCountInput() {
    override val icon = Res.drawable.count_step
    override val initialCount get() = element.tags["step_count"]?.toIntOrNull()
}
