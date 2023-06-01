package de.westnordost.streetcomplete.quests.building_colour

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.Text
import de.westnordost.streetcomplete.view.image_select.DisplayItem

fun BuildingColour.asItem(context: Context): DisplayItem<BuildingColour> =
    RoofColourDisplayItem(this, context)

class RoofColourDisplayItem(override val value: BuildingColour, context: Context) :
    DisplayItem<BuildingColour> {
    override val image: Image
    override val title: Text? = null
    override val description: Text? = null

    init {
        val color = Color.parseColor(value.androidValue ?: value.osmValue)
        val contrastColor = getBestContrast(context)
        val iconResId = R.drawable.ic_building_colour
        val drawable = context.getDrawable(iconResId)!!
        val matrix = ColorMatrix(
            floatArrayOf(
                color.red / 255f, 0f, contrastColor.red / 255f, 0f, 0f,
                color.green / 255f, 0f, contrastColor.green / 255f, 0f, 0f,
                color.blue / 255f, 0f, contrastColor.blue / 255f, 0f, 0f,
                1f, 1f, 1f, 1f, 0f
            )
        )
        drawable.colorFilter = ColorMatrixColorFilter(matrix)
        image = DrawableImage(drawable)
    }

    override fun hashCode(): Int = value.androidValue.hashCode()
    override fun equals(other: Any?): Boolean =
        (other is RoofColourDisplayItem) && (other.value.androidValue == value.androidValue)
}

private fun isDarkMode(context: Context): Boolean {
    val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}

private fun getBestContrast(context: Context): Int {
    return if (isDarkMode(context)) Color.LTGRAY else Color.DKGRAY
}
