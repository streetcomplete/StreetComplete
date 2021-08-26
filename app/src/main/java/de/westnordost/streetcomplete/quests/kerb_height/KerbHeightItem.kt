package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.kerb_height.KerbHeight.*
import de.westnordost.streetcomplete.view.image_select.Item

fun List<KerbHeight>.toItems() = this.map { it.asItem() }

fun KerbHeight.asItem(): Item<KerbHeight> = when(this) {
    RAISED -> Item(RAISED, R.drawable.kerb_height_raised, R.string.quest_kerb_height_raised)
    LOWERED -> Item(LOWERED, R.drawable.kerb_height_lowered, R.string.quest_kerb_height_lowered)
    FLUSH -> Item(FLUSH, R.drawable.kerb_height_flush, R.string.quest_kerb_height_flush)
    KERB_RAMP -> Item(KERB_RAMP, R.drawable.kerb_height_lowered_ramp, R.string.quest_kerb_height_lowered_ramp)
    NO_KERB -> Item(NO_KERB, R.drawable.kerb_height_no, R.string.quest_kerb_height_no)
}
