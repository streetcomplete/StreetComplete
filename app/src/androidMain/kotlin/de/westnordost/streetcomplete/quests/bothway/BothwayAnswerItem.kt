package de.westnordost.streetcomplete.quests.bothway

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
    BothwayAnswer.UPWARD -> R.string.quest_bothway_answer_upwards
    BothwayAnswer.DOWNWARD -> R.string.quest_bothway_answer_downwards
    BothwayAnswer.BOTHWAY -> R.string.quest_bothway_answer_bothway
}

//kept oneway icons, feel free to update it
private val BothwayAnswer.iconResId: Int get() = when (this) {
    BothwayAnswer.UPWARD -> R.drawable.ic_oneway_yes
    BothwayAnswer.DOWNWARD -> R.drawable.ic_oneway_yes_reverse
    BothwayAnswer.BOTHWAY -> R.drawable.ic_oneway_no
}
