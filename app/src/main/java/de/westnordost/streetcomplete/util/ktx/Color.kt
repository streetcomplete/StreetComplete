package de.westnordost.streetcomplete.util.ktx

import android.graphics.Color
import android.graphics.Color.argb
import androidx.annotation.ColorInt
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import kotlin.math.max

fun darken(color: Int, by: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    hsv[2] = hsv[2] * by
    return Color.HSVToColor(color.alpha, hsv)
}

fun addTransparency(@ColorInt color: Int, by: Float): Int =
    argb(max(0, color.alpha - (by * 255).toInt()), color.red, color.green, color.blue)

fun toARGBString(color: Int): String =
    "#" + color.alpha.toString(16).padStart(2, '0') +
        color.red.toString(16).padStart(2, '0') +
        color.green.toString(16).padStart(2, '0') +
        color.blue.toString(16).padStart(2, '0')
