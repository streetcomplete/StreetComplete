package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val StepsRamp.title: StringResource get() = when (this) {
    NONE ->       Res.string.quest_steps_ramp_none
    BICYCLE ->    Res.string.quest_steps_ramp_bicycle
    STROLLER ->   Res.string.quest_steps_ramp_stroller
    WHEELCHAIR -> Res.string.quest_steps_ramp_wheelchair
}

val StepsRamp.icon: DrawableResource get() = when (this) {
    NONE ->       Res.drawable.ramp_none
    BICYCLE ->    Res.drawable.ramp_bicycle
    STROLLER ->   Res.drawable.ramp_stroller
    WHEELCHAIR -> Res.drawable.ramp_wheelchair
}
