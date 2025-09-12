package de.westnordost.streetcomplete.quests.steps_ramp

import de.westnordost.streetcomplete.quests.steps_ramp.StepsRamp.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_steps_ramp_bicycle
import de.westnordost.streetcomplete.resources.quest_steps_ramp_none
import de.westnordost.streetcomplete.resources.quest_steps_ramp_stroller
import de.westnordost.streetcomplete.resources.quest_steps_ramp_wheelchair
import de.westnordost.streetcomplete.resources.ramp_bicycle
import de.westnordost.streetcomplete.resources.ramp_none
import de.westnordost.streetcomplete.resources.ramp_stroller
import de.westnordost.streetcomplete.resources.ramp_wheelchair
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
