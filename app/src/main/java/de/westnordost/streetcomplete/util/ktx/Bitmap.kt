package de.westnordost.streetcomplete.ktx

import android.graphics.Bitmap
import android.graphics.Matrix

fun Bitmap.flipHorizontally(): Bitmap {
    val matrix = Matrix().apply { postScale(-1f, 1f, width / 2f, width / 2f) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}
