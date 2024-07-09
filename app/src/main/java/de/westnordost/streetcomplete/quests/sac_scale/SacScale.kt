package de.westnordost.streetcomplete.quests.sac_scale

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.sac_scale.SacScale.*
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class SacScale(
    val osmValue: String,
    @DrawableRes val imageResId: Int,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
) {
    HIKING(
        osmValue = "hiking",
        imageResId = R.drawable.sac_scale_t1,
        titleResId = R.string.quest_sacScale_one,
        descriptionResId = R.string.quest_sacScale_one_description
    ),
    MOUNTAIN_HIKING(
        osmValue = "mountain_hiking",
        imageResId = R.drawable.sac_scale_t2,
        titleResId = R.string.quest_sacScale_two,
        descriptionResId = R.string.quest_sacScale_two_description
    ),
    DEMANDING_MOUNTAIN_HIKING(
        osmValue = "demanding_mountain_hiking",
        imageResId = R.drawable.sac_scale_t3,
        titleResId = R.string.quest_sacScale_three,
        descriptionResId = R.string.quest_sacScale_three_description
    ),
    ALPINE_HIKING(
        osmValue = "alpine_hiking",
        imageResId = R.drawable.sac_scale_t4,
        titleResId = R.string.quest_sacScale_four,
        descriptionResId = R.string.quest_sacScale_four_description
    ),
    DEMANDING_ALPINE_HIKING(
        osmValue = "demanding_alpine_hiking",
        imageResId = R.drawable.sac_scale_t5,
        titleResId = R.string.quest_sacScale_five,
        descriptionResId = R.string.quest_sacScale_five_description
    ),
    DIFFICULT_ALPINE_HIKING(
        osmValue = "difficult_alpine_hiking",
        imageResId = R.drawable.sac_scale_t6,
        titleResId = R.string.quest_sacScale_six,
        descriptionResId = R.string.quest_sacScale_six_description
    )
}

fun Collection<SacScale>.toItems() = map { it.asItem() }

fun SacScale.asItem(): GroupableDisplayItem<SacScale> {
    return Item(this, imageResId, titleResId, descriptionResId)
}
