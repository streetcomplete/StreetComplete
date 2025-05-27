package de.westnordost.streetcomplete.quests.incline_direction

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun Incline.asItem(context: Context, rotation: Float): DisplayItem<Incline> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(R.string.quest_steps_incline_up))
}

private val Incline.iconResId: Int get() = when (this) {
    Incline.UP -> R.drawable.ic_steps_incline_up
    Incline.UP_REVERSED -> R.drawable.ic_steps_incline_up_reversed
}
