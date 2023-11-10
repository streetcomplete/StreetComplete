package de.westnordost.streetcomplete.quests.trail_visibility

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.trail_visibility.TrailVisibility.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class TrailVisibility(val osmValue: String) {
    EXCELLENT("excellent"),
    GOOD("good"),
    INTERMEDIATE("intermediate"),
    BAD("bad"),
    HORRIBLE("horrible"),
    NO("no")
}
fun Collection<TrailVisibility>.toItems() = map { it.asItem() }

fun TrailVisibility.asItem(): GroupableDisplayItem<TrailVisibility> {
    return Item(this, titleId = titleResId, descriptionId = descriptionResId)
}

private val TrailVisibility.titleResId: Int get() = when (this) {
    EXCELLENT -> R.string.quest_trail_visibility_excellent
    GOOD -> R.string.quest_trail_visibility_good
    INTERMEDIATE -> R.string.quest_trail_visibility_intermediate
    BAD -> R.string.quest_trail_visibility_bad
    HORRIBLE -> R.string.quest_trail_visibility_horrible
    NO -> R.string.quest_trail_visibility_no
}

private val TrailVisibility.descriptionResId: Int? get() = when (this) {
    EXCELLENT -> R.string.quest_trail_visibility_excellent_description
    GOOD -> R.string.quest_trail_visibility_good_description
    INTERMEDIATE -> R.string.quest_trail_visibility_intermediate_description
    BAD -> R.string.quest_trail_visibility_bad_description
    HORRIBLE -> R.string.quest_trail_visibility_horrible_description
    NO -> R.string.quest_trail_visibility_no_description
    else -> null
}
