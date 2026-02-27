package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*

enum class MaxSpeedType {
    SIGN,
    ZONE,
    LIVING_STREET,
    ADVISORY,
    DEFAULT,
}

val MaxSpeedType.text get() = when (this) {
    MaxSpeedType.SIGN -> Res.string.quest_maxspeed_answer_sign
    MaxSpeedType.ZONE -> Res.string.quest_maxspeed_answer_zone2
    MaxSpeedType.LIVING_STREET -> Res.string.quest_maxspeed_answer_living_street
    MaxSpeedType.ADVISORY -> Res.string.quest_maxspeed_answer_advisory_speed_limit
    MaxSpeedType.DEFAULT -> Res.string.quest_maxspeed_answer_noSign2
}
