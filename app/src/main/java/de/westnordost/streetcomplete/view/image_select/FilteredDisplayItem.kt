package de.westnordost.streetcomplete.view.image_select

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.annotation.DrawableRes
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.Text

abstract class FilteredDisplayItem<T>(override val value: T, val context: Context) :
    DisplayItem<T> where T : OsmColour {
    override val title: Text? = null
    override val description: Text? = null

    @DrawableRes
    var iconResId: Int = 0

    override val image: Image
        get() {
            val color = Color.parseColor(value.androidValue ?: value.osmValue)
            val contrastColor = getBestContrast(context)
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
            return DrawableImage(drawable)
        }

    override fun hashCode(): Int = value.androidValue.hashCode()
    override fun equals(other: Any?): Boolean =
        (other is FilteredDisplayItem<*>) && (other.value.androidValue == value.androidValue)
}

private fun isDarkMode(context: Context): Boolean {
    val darkModeFlag = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
}

private fun getBestContrast(context: Context): Int {
    return if (isDarkMode(context)) Color.LTGRAY else Color.DKGRAY
}
