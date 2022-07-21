package de.westnordost.streetcomplete.util.ktx

import android.graphics.Color
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

fun toARGBString(color: Int): String =
    "#" + color.alpha.toString(16).padStart(2, '0') +
        color.red.toString(16).padStart(2, '0') +
        color.green.toString(16).padStart(2, '0') +
        color.blue.toString(16).padStart(2, '0')
