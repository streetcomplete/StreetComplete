package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.quests.foot.ProhibitedForPedestriansAnswer.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_accessible_for_pedestrians_allowed
import de.westnordost.streetcomplete.resources.quest_accessible_for_pedestrians_prohibited
import de.westnordost.streetcomplete.resources.quest_accessible_for_pedestrians_sidewalk
import org.jetbrains.compose.resources.StringResource

enum class ProhibitedForPedestriansAnswer {
    PROHIBITED,
    ALLOWED,
    ACTUALLY_HAS_SIDEWALK
}

val ProhibitedForPedestriansAnswer.text: StringResource get() = when (this) {
    PROHIBITED -> Res.string.quest_accessible_for_pedestrians_prohibited
    ALLOWED -> Res.string.quest_accessible_for_pedestrians_allowed
    ACTUALLY_HAS_SIDEWALK -> Res.string.quest_accessible_for_pedestrians_sidewalk
}
