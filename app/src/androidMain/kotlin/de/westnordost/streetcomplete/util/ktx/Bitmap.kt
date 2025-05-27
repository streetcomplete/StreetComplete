package de.westnordost.streetcomplete.util.ktx

import android.graphics.Bitmap
import android.graphics.Matrix
import de.westnordost.streetcomplete.util.sdf.convertToSdf
import kotlin.math.ceil

fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply { postScale(-1f, 1f, width / 2f, width / 2f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.toSdf(radius: Double = 8.0, cutoff: Double = 0.25): Bitmap {
    val buffer = ceil(radius * (1.0 - cutoff)).toInt()
    val w = width + 2 * buffer
    val h = height + 2 * buffer
    val pixels = IntArray(w * h)
    getPixels(pixels, w * buffer + buffer, w, 0, 0, width, height)
    convertToSdf(pixels, w, radius, cutoff)
    return Bitmap.createBitmap(pixels, w, pixels.size / w, Bitmap.Config.ARGB_8888)
}
