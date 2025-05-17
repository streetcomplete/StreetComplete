package de.westnordost.streetcomplete.util.ktx

import kotlin.math.abs
import kotlin.math.roundToInt

// conversion code rgb <-> hsv pretty much taken from https://www.tlbx.app/color-converter

data class RGB(val red: UByte, val green: UByte, val blue: UByte) {

    /** return as HSV */
    fun toHsv(): HSV {
        val r = red.toFloat() / 255f
        val g = green.toFloat() / 255f
        val b = blue.toFloat() / 255f
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        val h = when {
            delta == 0f -> 0f
            max == r ->    60f * ((g - b) / delta)
            max == g ->    60f * ((b - r) / delta) + 120f
            max == b ->    60f * ((r - g) / delta) + 240f
            else -> 0f
        }
        return HSV(
            hue = if (h < 0) h + 360f else h,
            saturation = if (max == 0f) 0f else delta / max,
            value = max
        )
    }

    /** return color as hexadecimal string "#rrggbbaa" */
    @OptIn(ExperimentalStdlibApi::class)
    fun toHexString(): String =
        "#" + red.toHexString() + green.toHexString() + blue.toHexString()
}

/** Creates RGB from string in the form "#rrggbb" */
@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
fun String.toRGB(): RGB {
    require(length == 7 || length == 9)
    require(get(0) == '#')

    val rgb = substring(1).hexToUByteArray()

    return RGB(rgb[0], rgb[1], rgb[2])
}

data class HSV(val hue: Float, val saturation: Float, val value: Float) {

    init {
        require(hue in 0f..360f) { "hue must be between 0..360 but was $hue" }
        require(saturation in 0f..1f) { "saturation must be between 0..1 but was $saturation" }
        require(value in 0f..1f) { "value must be between 0..1 but was $value" }
    }

    /** return as RGB */
    fun toRgb(): RGB {
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
        return RGB(
            red = (red * 255).roundToInt().toUByte(),
            green = (green * 255).roundToInt().toUByte(),
            blue = (blue * 255).roundToInt().toUByte()
        )
    }
}
