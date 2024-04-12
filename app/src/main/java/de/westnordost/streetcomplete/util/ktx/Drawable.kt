package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.style.ImageSpan
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable

fun Drawable.createBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap =
    createBitmap(width, height, Bitmap.Config.ARGB_8888).applyCanvas {
        setBounds(0, 0, this.width, this.height)
        draw(this)
    }

fun Drawable.asBitmapDrawable(resources: Resources, width: Int = intrinsicWidth, height: Int = intrinsicHeight): BitmapDrawable =
    if (this is BitmapDrawable) this else createBitmap(width, height).toDrawable(resources)

fun Drawable.asImageSpan(width: Int = intrinsicWidth, height: Int = intrinsicHeight): ImageSpan {
    this.mutate()
    this.setBounds(0, 0, width, height)
    // ALIGN_CENTER does not work correctly on SDK 29, see #3736
    val alignment = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) ImageSpan.ALIGN_CENTER else ImageSpan.ALIGN_BASELINE
    return ImageSpan(this, alignment)
}
