package de.westnordost.streetcomplete.util.color

// conversion code rgb <-> hsv pretty much taken from https://www.tlbx.app/color-converter

data class Rgb(val red: UByte, val green: UByte, val blue: UByte) {

    /** return as Hsv */
    fun toHsv(): Hsv {
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
        return Hsv(
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
fun String.toRgb(): Rgb {
    require(length == 7 || length == 9)
    require(get(0) == '#')

    val rgb = substring(1).hexToUByteArray()

    return Rgb(rgb[0], rgb[1], rgb[2])
}
