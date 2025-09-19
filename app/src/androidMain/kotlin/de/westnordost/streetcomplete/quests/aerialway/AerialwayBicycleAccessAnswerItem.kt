package de.westnordost.streetcomplete.quests.aerialway

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun AerialwayBicycleAccessAnswer.asItem(context: Context): DisplayItem<AerialwayBicycleAccessAnswer> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val AerialwayBicycleAccessAnswer.titleResId: Int get() = when (this) {
    AerialwayBicycleAccessAnswer.YES -> R.string.quest_aerialway_bicycle_yes
    AerialwayBicycleAccessAnswer.SUMMER -> R.string.quest_aerialway_bicycle_summer
    AerialwayBicycleAccessAnswer.NO -> R.string.quest_aerialway_bicycle_no
}

private val AerialwayBicycleAccessAnswer.iconResId: Int get() = when (this) {
    AerialwayBicycleAccessAnswer.YES -> R.drawable.ic_oneway_yes
    AerialwayBicycleAccessAnswer.SUMMER -> R.drawable.ic_oneway_yes_reverse
    AerialwayBicycleAccessAnswer.NO -> R.drawable.ic_oneway_no
}
