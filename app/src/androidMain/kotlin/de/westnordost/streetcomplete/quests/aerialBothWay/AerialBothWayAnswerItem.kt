package de.westnordost.streetcomplete.quests.aerialBothWay

import android.content.Context
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.RotatedCircleDrawable
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun AerialBothWayAnswer.asItem(context: Context, rotation: Float): DisplayItem<AerialBothWayAnswer> {
    val drawable = RotatedCircleDrawable(context.getDrawable(iconResId)!!)
    drawable.rotation = rotation
    return Item2(this, DrawableImage(drawable), ResText(titleResId))
}

private val AerialBothWayAnswer.titleResId: Int get() = when (this) {
    AerialBothWayAnswer.UPWARD -> R.string.quest_bothway_answer_upwards
    AerialBothWayAnswer.DOWNWARD -> R.string.quest_bothway_answer_downwards
    AerialBothWayAnswer.BOTHWAY -> R.string.quest_bothway_answer_bothway
}

// kept oneway icons, feel free to update it
private val AerialBothWayAnswer.iconResId: Int get() = when (this) {
    AerialBothWayAnswer.UPWARD -> R.drawable.ic_oneway_yes
    AerialBothWayAnswer.DOWNWARD -> R.drawable.ic_oneway_yes_reverse
    AerialBothWayAnswer.BOTHWAY -> R.drawable.ic_oneway_no
}
