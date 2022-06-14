package de.westnordost.streetcomplete.quests.steps_incline

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun StepsIncline.toItem(context: Context, rotation: Float): DisplayItem<StepsIncline> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val StepsIncline.titleResId: Int get() = R.string.quest_steps_incline_up

private val StepsIncline.iconResId: Int get() = when (this) {
    StepsIncline.UP -> R.drawable.ic_steps_incline_up
    StepsIncline.UP_REVERSED -> R.drawable.ic_steps_incline_up_reversed
}
