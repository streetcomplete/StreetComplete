package de.westnordost.streetcomplete.quests.max_speed

import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_advisory_speed_limit
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_living_street
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_noSign2
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_nsl
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_sign
import de.westnordost.streetcomplete.resources.quest_maxspeed_answer_zone2

enum class MaxSpeedType {
    SIGN,
    ZONE,
    LIVING_STREET,
    ADVISORY,
    NO_SIGN,
    NSL
}
