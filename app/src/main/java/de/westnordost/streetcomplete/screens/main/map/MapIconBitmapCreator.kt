package de.westnordost.streetcomplete.screens.main.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import androidx.annotation.DrawableRes
import androidx.core.graphics.toRect
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.util.ktx.createBitmap
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.toSdf
import kotlin.math.ceil

fun createPinBitmap(
    context: Context,
    @DrawableRes iconResId: Int
): Bitmap {
    val scale = 1f
    val size = context.resources.dpToPx(71 * scale)
    val sizeInt = ceil(size).toInt()
    val iconSize = context.resources.dpToPx(48 * scale)
    val iconPinOffset = context.resources.dpToPx(2 * scale)
    val pinTopRightPadding = context.resources.dpToPx(5 * scale)

    val pin = context.getDrawable(R.drawable.pin)!!
    val pinShadow = context.getDrawable(R.drawable.pin_shadow)!!

    val pinWidth = (size - pinTopRightPadding) * pin.intrinsicWidth / pin.intrinsicHeight
    val pinXOffset = size - pinTopRightPadding - pinWidth

    val bitmap = Bitmap.createBitmap(sizeInt, sizeInt, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    pinShadow.setBounds(0, 0, sizeInt, sizeInt)
    pinShadow.draw(canvas)
    pin.bounds = RectF(
        pinXOffset,
        pinTopRightPadding,
        size - pinTopRightPadding,
        size
    ).toRect()
    pin.draw(canvas)
    val questIcon = context.getDrawable(iconResId)!!
    questIcon.bounds = RectF(
        pinXOffset + iconPinOffset,
        pinTopRightPadding + iconPinOffset,
        pinXOffset + iconPinOffset + iconSize,
        pinTopRightPadding + iconPinOffset + iconSize
    ).toRect()
    questIcon.draw(canvas)
    return bitmap
}

fun createIconBitmap(
    context: Context,
    @DrawableRes iconResId: Int,
    createSdf: Boolean = false,
    maxSizeDp: Int = 48
): Bitmap {
    val drawable = context.getDrawable(iconResId)!!
    val maxIconSize = context.resources.dpToPx(maxSizeDp).toInt()
    val bitmap = drawable.createBitmap(
        width = drawable.intrinsicWidth.coerceAtMost(maxIconSize),
        height = drawable.intrinsicHeight.coerceAtMost(maxIconSize),
    )
    if (!createSdf) return bitmap
    return bitmap.toSdf(radius = context.resources.dpToPx(8.0).toDouble())
}
