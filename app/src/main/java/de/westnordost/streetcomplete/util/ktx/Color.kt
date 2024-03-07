package de.westnordost.streetcomplete.util.ktx

import kotlin.math.abs
import kotlin.math.roundToInt

// conversion code rgb <-> hsv pretty much taken from https://www.tlbx.app/color-converter

data class RGBA(val red: UByte, val green: UByte, val blue: UByte, val alpha: UByte = 255u) {

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
        "#" + red.toHexString() + green.toHexString() + blue.toHexString() + alpha.toHexString()
}

/** Creates RGBA from string in the form "#rrggbb" or "#rrggbbaa" */
@OptIn(ExperimentalStdlibApi::class, ExperimentalUnsignedTypes::class)
fun String.toRGBA(): RGBA {
    require(length == 7 || length == 9)
    require(get(0) == '#')

    val rgba = substring(1).hexToUByteArray()

    return RGBA(
        red = rgba[0],
        green = rgba[1],
        blue = rgba[2],
        alpha = if (rgba.size > 3) rgba[3] else 255u,
    )
}

data class HSV(val hue: Float, val saturation: Float, val value: Float) {

    init {
        require(hue in 0f .. 360f) { "hue must be between 0..360 but was $hue" }
        require(saturation in 0f .. 1f) { "saturation must be between 0..1 but was $saturation" }
        require(value in 0f .. 1f) { "value must be between 0..1 but was $value" }
    }

    /** return as RGB + alpha */
    fun toRgba(alpha: UByte = 255u): RGBA {
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
        return RGBA(
            red = (red * 255).roundToInt().toUByte(),
            green = (green * 255).roundToInt().toUByte(),
            blue = (blue * 255).roundToInt().toUByte(),
            alpha = alpha
        )
    }
}
