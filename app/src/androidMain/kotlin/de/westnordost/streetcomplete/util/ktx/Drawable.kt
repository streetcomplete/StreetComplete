package de.westnordost.streetcomplete.util.ktx

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.core.graphics.applyCanvas
import androidx.core.graphics.createBitmap

fun Drawable.createBitmap(width: Int = intrinsicWidth, height: Int = intrinsicHeight): Bitmap =
    createBitmap(width, height, Bitmap.Config.ARGB_8888).applyCanvas {
        setBounds(0, 0, this.width, this.height)
        draw(this)
    }
