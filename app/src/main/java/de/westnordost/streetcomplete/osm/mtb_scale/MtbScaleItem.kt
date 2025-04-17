package de.westnordost.streetcomplete.osm.mtb_scale

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.GroupableDisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun MtbScale.asItem(): GroupableDisplayItem<MtbScale> =
    Item(this, imageResId, titleResId, descriptionResId)

private val MtbScale.titleResId: Int? @StringRes get() = when (value) {
    0 -> R.string.overlay_mtb_scale_0
    1 -> R.string.overlay_mtb_scale_1
    2 -> R.string.overlay_mtb_scale_2
    3 -> R.string.overlay_mtb_scale_3
    4 -> R.string.overlay_mtb_scale_4
    5 -> R.string.overlay_mtb_scale_5
    6 -> R.string.overlay_mtb_scale_6
    else -> null
}

private val MtbScale.descriptionResId: Int? @StringRes get() = when (value) {
    0 -> R.string.overlay_mtb_scale_0_description
    1 -> R.string.overlay_mtb_scale_1_description
    2 -> R.string.overlay_mtb_scale_2_description
    3 -> R.string.overlay_mtb_scale_3_description
    4 -> R.string.overlay_mtb_scale_4_description
    5 -> R.string.overlay_mtb_scale_5_description
    6 -> R.string.overlay_mtb_scale_6_description
    else -> null
}

private val MtbScale.imageResId: Int? @DrawableRes get() = when (value) {
    0 -> R.drawable.mtb_scale_0
    1 -> R.drawable.mtb_scale_1
    2 -> R.drawable.mtb_scale_2
    3 -> R.drawable.mtb_scale_3
    4 -> R.drawable.mtb_scale_4
    5 -> R.drawable.mtb_scale_5
    6 -> null
    else -> null
}
