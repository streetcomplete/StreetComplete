package de.westnordost.streetcomplete.util.color

import kotlin.math.abs
import kotlin.math.roundToInt

// conversion code rgb <-> hsv pretty much taken from https://www.tlbx.app/color-converter

data class Hsv(val hue: Float, val saturation: Float, val value: Float) {

    init {
        require(hue in 0f..360f) { "hue must be between 0..360 but was $hue" }
        require(saturation in 0f..1f) { "saturation must be between 0..1 but was $saturation" }
        require(value in 0f..1f) { "value must be between 0..1 but was $value" }
    }

    /** return as Rgb */
    fun toRgb(): Rgb {
        val h = hue / 60f
        val chroma = value * saturation
        val x = chroma * (1 - abs(h % 2 - 1))
        val m = value - chroma
        val (red, green, blue) = when {
            h <= 1 -> Triple(chroma + m, x + m, 0 + m)
            h <= 2 -> Triple(x + m, chroma + m, 0 + m)
            h <= 3 -> Triple(0 + m, chroma + m, x + m)
            h <= 4 -> Triple(0 + m, x + m, chroma + m)
            h <= 5 -> Triple(x + m, 0 + m, chroma + m)
            h <= 6 -> Triple(chroma + m, 0 + m, x + m)
            else ->   Triple(0f, 0f, 0f)
        }
        return Rgb(
            red = (red * 255).roundToInt().toUByte(),
            green = (green * 255).roundToInt().toUByte(),
            blue = (blue * 255).roundToInt().toUByte()
        )
    }
}
