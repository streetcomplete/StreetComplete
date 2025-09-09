package de.westnordost.streetcomplete.quests.incline_direction

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.steps_incline_up
import de.westnordost.streetcomplete.resources.steps_incline_up_reversed
import org.jetbrains.compose.resources.DrawableResource

val Incline.icon: DrawableResource get() = when (this) {
    Incline.UP -> Res.drawable.steps_incline_up
    Incline.UP_REVERSED -> Res.drawable.steps_incline_up_reversed
}

// TODO title = Res.string.quest_steps_incline_up
// TODO rotation
