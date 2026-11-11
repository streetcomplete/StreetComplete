package de.westnordost.streetcomplete.util.ktx

/** Checks whether the number of Unicode code points in this (UTF-16) char sequence is smaller or
 *  equal than the given [maxLength] */
fun CharSequence.isCodePointCountSmallerOrEqualThan(maxLength: Int): Boolean {
    // shortcut that covers strings that are guaranteed to be short enough
    if (length <= maxLength) return true

    val highSurrogate = '\uD800'..'\uDBFF'
    val lowSurrogate = '\uDC00'..'\uDFFF'
    var count = 0
    var i = 0
    while (i < length) {
        if (this[i] in highSurrogate && i + 1 < length && this[i + 1] in lowSurrogate) {
            ++i
        }
        // early return when count is exceeded
        if (++count > maxLength) return false
        ++i
    }
    return true
}
