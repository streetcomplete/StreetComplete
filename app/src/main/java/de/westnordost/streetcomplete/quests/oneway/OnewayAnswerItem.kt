package de.westnordost.streetcomplete.quests.oneway

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun OnewayAnswer.asItem(context: Context, rotation: Float): DisplayItem<OnewayAnswer> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val OnewayAnswer.titleResId: Int get() = when (this) {
    OnewayAnswer.FORWARD -> R.string.quest_oneway2_dir
    OnewayAnswer.BACKWARD -> R.string.quest_oneway2_dir
    OnewayAnswer.NO_ONEWAY -> R.string.quest_oneway2_no_oneway
}

private val OnewayAnswer.iconResId: Int get() = when (this) {
    OnewayAnswer.FORWARD -> R.drawable.ic_oneway_yes
    OnewayAnswer.BACKWARD -> R.drawable.ic_oneway_yes_reverse
    OnewayAnswer.NO_ONEWAY -> R.drawable.ic_oneway_no
}
