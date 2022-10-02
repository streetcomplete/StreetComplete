package de.westnordost.streetcomplete.util.ktx

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

fun darken(color: Int, by: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] = hsv[2] * by
    return Color.HSVToColor(color.alpha, hsv)
}

fun alpha(@ColorInt color: Int, alpha: Float): Int =
    Color.argb((alpha.coerceIn(0f, 1f)*255).toInt(), color.red, color.green, color.blue)

fun toARGBString(color: Int): String =
    "#" + color.alpha.toString(16).padStart(2, '0') +
        color.red.toString(16).padStart(2, '0') +
        color.green.toString(16).padStart(2, '0') +
        color.blue.toString(16).padStart(2, '0')
