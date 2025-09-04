package de.westnordost.streetcomplete.quests.moped

import de.westnordost.streetcomplete.quests.moped.MopedAccessAnswer.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_moped_access_allowed
import de.westnordost.streetcomplete.resources.quest_moped_access_designated
import de.westnordost.streetcomplete.resources.quest_moped_access_forbidden
import org.jetbrains.compose.resources.StringResource

enum class MopedAccessAnswer {
    DESIGNATED,
    FORBIDDEN,
    NO_SIGN,
}

val MopedAccessAnswer.text: StringResource get() = when (this) {
    DESIGNATED -> Res.string.quest_moped_access_designated
    FORBIDDEN -> Res.string.quest_moped_access_forbidden
    NO_SIGN -> Res.string.quest_moped_access_allowed
}
