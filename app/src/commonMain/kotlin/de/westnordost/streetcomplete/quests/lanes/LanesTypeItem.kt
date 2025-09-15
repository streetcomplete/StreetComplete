package de.westnordost.streetcomplete.quests.lanes

import de.westnordost.streetcomplete.quests.lanes.LanesType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.lanes_marked
import de.westnordost.streetcomplete.resources.lanes_marked_odd
import de.westnordost.streetcomplete.resources.lanes_unmarked
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_odd2
import de.westnordost.streetcomplete.resources.quest_lanes_answer_noLanes
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val LanesType.icon: DrawableResource get() = when (this) {
    MARKED -> Res.drawable.lanes_marked
    UNMARKED -> Res.drawable.lanes_unmarked
    MARKED_SIDES -> Res.drawable.lanes_marked_odd
}

val LanesType.title: StringResource get() = when (this) {
    MARKED -> Res.string.quest_lanes_answer_lanes
    UNMARKED -> Res.string.quest_lanes_answer_noLanes
    MARKED_SIDES -> Res.string.quest_lanes_answer_lanes_odd2
}
