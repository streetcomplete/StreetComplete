package de.westnordost.streetcomplete.quests.smoking

import de.westnordost.streetcomplete.quests.smoking.SmokingAllowed.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_smoking_no
import de.westnordost.streetcomplete.resources.quest_smoking_outside
import de.westnordost.streetcomplete.resources.quest_smoking_separated
import de.westnordost.streetcomplete.resources.quest_smoking_yes
import org.jetbrains.compose.resources.StringResource

enum class SmokingAllowed(val osmValue: String) {
    YES("yes"),
    OUTSIDE("outside"),
    NO("no"),
    SEPARATED("separated"),
}

val SmokingAllowed.text: StringResource get() = when (this) {
    YES -> Res.string.quest_smoking_yes
    OUTSIDE -> Res.string.quest_smoking_outside
    NO -> Res.string.quest_smoking_no
    SEPARATED -> Res.string.quest_smoking_separated
}
