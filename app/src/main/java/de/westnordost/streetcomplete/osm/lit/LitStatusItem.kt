package de.westnordost.streetcomplete.osm.lit

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.lit.LitStatus.AUTOMATIC
import de.westnordost.streetcomplete.osm.lit.LitStatus.NIGHT_AND_DAY
import de.westnordost.streetcomplete.osm.lit.LitStatus.NO
import de.westnordost.streetcomplete.osm.lit.LitStatus.UNSUPPORTED
import de.westnordost.streetcomplete.osm.lit.LitStatus.YES
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun LitStatus.asItem(): DisplayItem<LitStatus> =
    Item(this, iconResId, titleResId)

private val LitStatus.iconResId: Int get() = when (this) {
    YES -> R.drawable.ic_lit_yes
    NO -> R.drawable.ic_lit_no
    AUTOMATIC -> R.drawable.ic_lit_automatic
    NIGHT_AND_DAY -> R.drawable.ic_lit_24_7
    UNSUPPORTED -> R.drawable.ic_lit_unsupported
}

private val LitStatus.titleResId: Int get() = when (this) {
    YES -> R.string.lit_value_yes
    NO -> R.string.lit_value_no
    AUTOMATIC -> R.string.lit_value_automatic
    NIGHT_AND_DAY -> R.string.lit_value_24_7
    UNSUPPORTED -> R.string.lit_value_unsupported
}
