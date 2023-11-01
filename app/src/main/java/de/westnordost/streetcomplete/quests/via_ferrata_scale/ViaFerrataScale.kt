package de.westnordost.streetcomplete.quests.via_ferrata_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.via_ferrata_scale.ViaFerrataScale.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class ViaFerrataScale(val osmValue: String) {
    ZERO("0"),
    ONE("1"),
    TWO("2"),
    THREE("3"),
    FOUR("4"),
    FIVE("5"),
    SIX("6")
}
fun Collection<ViaFerrataScale>.toItems() = map { it.asItem() }

fun ViaFerrataScale.asItem(): GroupableDisplayItem<ViaFerrataScale> {
    return Item(this, imageResId, titleResId, descriptionResId)
}

private val ViaFerrataScale.imageResId: Int get() = when (this) {
    ZERO -> R.drawable.via_ferrata_scale_0
    ONE -> R.drawable.via_ferrata_scale_1
    TWO -> R.drawable.via_ferrata_scale_2
    THREE -> R.drawable.via_ferrata_scale_3
    FOUR -> R.drawable.via_ferrata_scale_4
    FIVE -> R.drawable.via_ferrata_scale_5
    SIX -> R.drawable.via_ferrata_scale_6
}

private val ViaFerrataScale.titleResId: Int get() = when (this) {
    ZERO -> R.string.quest_viaFerrataScale_zero
    ONE -> R.string.quest_viaFerrataScale_one
    TWO -> R.string.quest_viaFerrataScale_two
    THREE -> R.string.quest_viaFerrataScale_three
    FOUR -> R.string.quest_viaFerrataScale_four
    FIVE -> R.string.quest_viaFerrataScale_five
    SIX -> R.string.quest_viaFerrataScale_six
}

private val ViaFerrataScale.descriptionResId: Int? get() = when (this) {
    ZERO -> R.string.quest_viaFerrataScale_zero_description
    ONE -> R.string.quest_viaFerrataScale_one_description
    TWO -> R.string.quest_viaFerrataScale_two_description
    THREE -> R.string.quest_viaFerrataScale_three_description
    FOUR -> R.string.quest_viaFerrataScale_four_description
    FIVE -> R.string.quest_viaFerrataScale_five_description
    SIX -> R.string.quest_viaFerrataScale_six_description
    else -> null
}
