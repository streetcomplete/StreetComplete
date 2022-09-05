package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.BICYCLE
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.NONE
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.STROLLER
import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.WHEELCHAIR
import de.westnordost.streetcomplete.view.image_select.Item

fun StepsRamp.asItem() = Item(this, iconResId, titleResId)

private val StepsRamp.titleResId: Int get() = when (this) {
    NONE ->       R.string.quest_steps_ramp_none
    BICYCLE ->    R.string.quest_steps_ramp_bicycle
    STROLLER ->   R.string.quest_steps_ramp_stroller
    WHEELCHAIR -> R.string.quest_steps_ramp_wheelchair
}

private val StepsRamp.iconResId: Int get() = when (this) {
    NONE ->       R.drawable.ramp_none
    BICYCLE ->    R.drawable.ramp_bicycle
    STROLLER ->   R.drawable.ramp_stroller
    WHEELCHAIR -> R.drawable.ramp_wheelchair
}
