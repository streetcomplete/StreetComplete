package de.westnordost.streetcomplete.quests.mtb_scale

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE0
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE1
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE2
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE3
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE4
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE5
import de.westnordost.streetcomplete.quests.mtb_scale.MtbScale.SCALE6
import de.westnordost.streetcomplete.view.image_select.Item

fun MtbScale.asItem() = Item(this, iconResId, titleResId)

private val MtbScale.titleResId: Int get() = when (this) {
    SCALE0 -> R.string.quest_mtb_scale0
    SCALE1 -> R.string.quest_mtb_scale1
    SCALE2 -> R.string.quest_mtb_scale2
    SCALE3 -> R.string.quest_mtb_scale3
    SCALE4 -> R.string.quest_mtb_scale4
    SCALE5 -> R.string.quest_mtb_scale5
    SCALE6 -> R.string.quest_mtb_scale6
}

private val MtbScale.iconResId: Int get() = when (this) {
    SCALE0 -> R.drawable.mtb_scale0
    SCALE1 -> R.drawable.mtb_scale1
    SCALE2 -> R.drawable.mtb_scale2
    SCALE3 -> R.drawable.mtb_scale3
    SCALE4 -> R.drawable.mtb_scale4
    SCALE5 -> R.drawable.mtb_scale5
    SCALE6 -> R.drawable.mtb_scale6
}
