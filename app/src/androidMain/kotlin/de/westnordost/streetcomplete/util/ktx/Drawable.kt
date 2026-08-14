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
