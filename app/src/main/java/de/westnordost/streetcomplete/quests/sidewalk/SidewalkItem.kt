package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.StreetSideDisplayItem
import de.westnordost.streetcomplete.quests.sidewalk.Sidewalk.*

val Sidewalk.iconResId get() = when(this) {
    YES -> R.drawable.ic_sidewalk_yes
    NO -> R.drawable.ic_sidewalk_no
    SEPARATE -> R.drawable.ic_sidewalk_separate
}

val Sidewalk.imageResId get() = when(this) {
    YES -> R.drawable.ic_sidewalk_puzzle_yes
    NO -> R.drawable.ic_sidewalk_puzzle_no
    SEPARATE -> R.drawable.ic_sidewalk_puzzle_no
}

val Sidewalk.floatingIconResId get() = when(this) {
    SEPARATE -> R.drawable.ic_sidewalk_separate_floating
    else -> null
}

val Sidewalk.titleResId get() = when(this) {
    YES -> R.string.quest_sidewalk_value_yes
    NO -> R.string.quest_sidewalk_value_no
    SEPARATE -> R.string.quest_sidewalk_value_separate
}

fun Sidewalk.asStreetSideDisplayItem(): StreetSideDisplayItem<Sidewalk> = TODO()
