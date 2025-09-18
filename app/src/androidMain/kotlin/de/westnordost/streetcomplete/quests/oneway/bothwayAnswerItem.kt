package de.westnordost.streetcomplete.quests.oneway

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun BothwayAnswer.asItem(context: Context, rotation: Float): DisplayItem<BothwayAnswer> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val BothwayAnswer.titleResId: Int get() = when (this) {
    BothwayAnswer.FORWARD -> R.string.quest_oneway2_dir
    BothwayAnswer.BACKWARD -> R.string.quest_oneway2_dir
    BothwayAnswer.NO_ONEWAY -> R.string.quest_oneway2_no_oneway
}

private val BothwayAnswer.iconResId: Int get() = when (this) {
    BothwayAnswer.FORWARD -> R.drawable.ic_oneway_yes
    BothwayAnswer.BACKWARD -> R.drawable.ic_oneway_yes_reverse
    BothwayAnswer.NO_ONEWAY -> R.drawable.ic_oneway_no
}
