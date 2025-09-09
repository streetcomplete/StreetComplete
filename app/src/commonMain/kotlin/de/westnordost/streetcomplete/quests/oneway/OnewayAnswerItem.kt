package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.streetcomplete.quests.oneway.OnewayAnswer.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.oneway_no
import de.westnordost.streetcomplete.resources.oneway_yes
import de.westnordost.streetcomplete.resources.oneway_yes_reverse
import de.westnordost.streetcomplete.resources.quest_oneway2_dir
import de.westnordost.streetcomplete.resources.quest_oneway2_no_oneway
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val OnewayAnswer.title: StringResource get() = when (this) {
    FORWARD -> Res.string.quest_oneway2_dir
    BACKWARD -> Res.string.quest_oneway2_dir
    NO_ONEWAY -> Res.string.quest_oneway2_no_oneway
}

val OnewayAnswer.icon: DrawableResource get() = when (this) {
    FORWARD -> Res.drawable.oneway_yes
    BACKWARD -> Res.drawable.oneway_yes_reverse
    NO_ONEWAY -> Res.drawable.oneway_no
}
