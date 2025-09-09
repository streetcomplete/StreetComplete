package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.quests.tracktype.Tracktype.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_tracktype_grade1
import de.westnordost.streetcomplete.resources.quest_tracktype_grade2
import de.westnordost.streetcomplete.resources.quest_tracktype_grade3
import de.westnordost.streetcomplete.resources.quest_tracktype_grade4
import de.westnordost.streetcomplete.resources.quest_tracktype_grade5
import de.westnordost.streetcomplete.resources.tracktype_grade1
import de.westnordost.streetcomplete.resources.tracktype_grade2
import de.westnordost.streetcomplete.resources.tracktype_grade3
import de.westnordost.streetcomplete.resources.tracktype_grade4
import de.westnordost.streetcomplete.resources.tracktype_grade5
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Tracktype.title: StringResource get() = when (this) {
    GRADE1 -> Res.string.quest_tracktype_grade1
    GRADE2 -> Res.string.quest_tracktype_grade2
    GRADE3 -> Res.string.quest_tracktype_grade3
    GRADE4 -> Res.string.quest_tracktype_grade4
    GRADE5 -> Res.string.quest_tracktype_grade5
}

val Tracktype.icon: DrawableResource get() = when (this) {
    GRADE1 -> Res.drawable.tracktype_grade1
    GRADE2 -> Res.drawable.tracktype_grade2
    GRADE3 -> Res.drawable.tracktype_grade3
    GRADE4 -> Res.drawable.tracktype_grade4
    GRADE5 -> Res.drawable.tracktype_grade5
}
