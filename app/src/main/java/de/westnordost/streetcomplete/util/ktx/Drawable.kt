package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.style.ImageSpan
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.get
import androidx.core.graphics.set
import kotlin.math.min

fun Drawable.createBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap {
    return createBitmap(width, height, Bitmap.Config.ARGB_8888).applyCanvas {
        setBounds(0, 0, this.width, this.height)
        draw(this)
    }
}

fun Drawable.asBitmapDrawable(resources: Resources, width: Int = intrinsicWidth, height: Int = intrinsicHeight): BitmapDrawable =
    if (this is BitmapDrawable) this else createBitmap(width, height).toDrawable(resources)

fun Drawable.asBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap =
    if (this is BitmapDrawable) bitmap else createBitmap(width, height)

fun Drawable.asImageSpan(width: Int = intrinsicWidth, height: Int = intrinsicHeight): ImageSpan {
    this.mutate()
    this.setBounds(0, 0, width, height)
    // ALIGN_CENTER does not work correctly on SDK 29, see #3736
    val alignment = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) ImageSpan.ALIGN_CENTER else ImageSpan.ALIGN_BASELINE
    return ImageSpan(this, alignment)
}

fun Drawable.createBitmapWithWhiteBorder(border: Int, width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap {
    val foreground = Bitmap.createBitmap(width + border * 2, height + border * 2, Bitmap.Config.ARGB_8888)
    val foregroundCanvas = Canvas(foreground)
    setBounds(border, border, border + width, border + height)
    draw(foregroundCanvas)
    val manhattan = calculateManhattanDistance(foreground)

    val background = Bitmap.createBitmap(width + border * 2, height + border * 2, Bitmap.Config.ARGB_8888)
    for (x in 0 until foreground.width) {
        for (y in 0 until foreground.height) {
            if (manhattan[x][y] <= border) {
                background[x, y] = Color.argb(255, 255, 255, 255)
            }
        }
    }
    val canvas = Canvas(background)
    setBounds(border, border, border + width, border + height)
    draw(canvas)
    return background
}

private fun calculateManhattanDistance(bitmap: Bitmap): Array<IntArray> {
    val m = Array(bitmap.width) { IntArray(bitmap.height) }
    for (x in 0 until bitmap.width) {
        for (y in 0 until bitmap.height) {
            if (Color.alpha(bitmap[x, y]) > 0) {
                m[x][y] = 0
            } else {
                m[x][y] = bitmap.width + bitmap.height
                if (x > 0) m[x][y] = min(m[x][y], m[x - 1][y] + 1)
                if (y > 0) m[x][y] = min(m[x][y], m[x][y - 1] + 1)
            }
        }
    }
    for (x in m.indices.reversed()) {
        for (y in m[x].indices.reversed()) {
            if (x + 1 < m.size)    m[x][y] = min(m[x][y], m[x + 1][y] + 1)
            if (y + 1 < m[x].size) m[x][y] = min(m[x][y], m[x][y + 1] + 1)
        }
    }
    return m
}
