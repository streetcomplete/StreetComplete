package de.westnordost.streetcomplete.overlays.mtb_scale

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R

import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

enum class MtbScale(
    val osmValue: String,
    @DrawableRes val imageResId: Int?,
    @StringRes val titleResId: Int,
    @StringRes val descriptionResId: Int,
    val color:String,
) {
    ZERO(
        osmValue = "0",
        imageResId = R.drawable.mtb_scale_0,
        titleResId = R.string.overlay_mtbScale_zero,
        descriptionResId = R.string.overlay_mtbScale_zero_description,
        color = "#DBECC0"
    ),
    ONE(
        osmValue = "1",
        imageResId = R.drawable.mtb_scale_1,
        titleResId = R.string.overlay_mtbScale_one,
        descriptionResId = R.string.overlay_mtbScale_one_description,
        color = "#8CC63E"
    ),
    TWO(
        osmValue = "2",
        imageResId = R.drawable.mtb_scale_2,
        titleResId = R.string.overlay_mtbScale_two,
        descriptionResId = R.string.overlay_mtbScale_two_description,
        color = "#00B2E6"
    ),
    THREE(
        osmValue = "3",
        imageResId = R.drawable.mtb_scale_3,
        titleResId = R.string.overlay_mtbScale_three,
        descriptionResId = R.string.overlay_mtbScale_three_description,
        color = "#FECB1B"
    ),
    FOUR(
        osmValue = "4",
        imageResId = R.drawable.mtb_scale_4,
        titleResId = R.string.overlay_mtbScale_four,
        descriptionResId = R.string.overlay_mtbScale_four_description,
        color = "#F47922"
    ),
    FIVE(
        osmValue = "5",
        imageResId = R.drawable.mtb_scale_5,
        titleResId = R.string.overlay_mtbScale_five,
        descriptionResId = R.string.overlay_mtbScale_five_description,
        color = "#874D99"
    ),
    SIX(
        osmValue = "6",
        imageResId = R.drawable.mtb_scale_6,
        titleResId = R.string.overlay_mtbScale_six,
        descriptionResId = R.string.overlay_mtbScale_six_description,
        color = "#000000"
    )
}

fun Collection<MtbScale>.toItems() = map { it.asItem() }

fun MtbScale.asItem(): GroupableDisplayItem<MtbScale> {
    return Item(
        this,
        drawableId = imageResId,
        titleId = titleResId,
        descriptionId = descriptionResId
    )
}
