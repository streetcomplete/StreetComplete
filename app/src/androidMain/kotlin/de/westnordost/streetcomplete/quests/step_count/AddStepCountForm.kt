package de.westnordost.streetcomplete.quests.step_count

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AAddCountInput

class AddStepCountForm : AAddCountInput() {
    override val iconId = R.drawable.ic_step
    override val initialCount get() = element.tags["step_count"]?.toIntOrNull()
}
