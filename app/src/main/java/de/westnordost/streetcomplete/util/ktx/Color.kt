package de.westnordost.streetcomplete.util.ktx

import android.graphics.Color

fun darken(color: Int, by: Float): Int {
    val hsv = FloatArray(3)
    Color.colorToHSV(color, hsv)
    val a = Color.alpha(color)
    hsv[2] = hsv[2] * by
    return Color.HSVToColor(a, hsv)
}

fun toARGBString(color: Int): String =
    "#" + Color.alpha(color).toString(16).padStart(2, '0') +
        Color.red(color).toString(16).padStart(2, '0') +
        Color.green(color).toString(16).padStart(2, '0') +
        Color.blue(color).toString(16).padStart(2, '0')
