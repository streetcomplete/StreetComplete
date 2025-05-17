package de.westnordost.streetcomplete.quests.tracktype

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE1
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE2
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE3
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE4
import de.westnordost.streetcomplete.quests.tracktype.Tracktype.GRADE5
import de.westnordost.streetcomplete.view.image_select.Item

fun Tracktype.asItem() = Item(this, iconResId, titleResId)

private val Tracktype.titleResId: Int get() = when (this) {
    GRADE1 -> R.string.quest_tracktype_grade1
    GRADE2 -> R.string.quest_tracktype_grade2a
    GRADE3 -> R.string.quest_tracktype_grade3a
    GRADE4 -> R.string.quest_tracktype_grade4
    GRADE5 -> R.string.quest_tracktype_grade5
}

private val Tracktype.iconResId: Int get() = when (this) {
    GRADE1 -> R.drawable.tracktype_grade1
    GRADE2 -> R.drawable.tracktype_grade2
    GRADE3 -> R.drawable.tracktype_grade3
    GRADE4 -> R.drawable.tracktype_grade4
    GRADE5 -> R.drawable.tracktype_grade5
}
