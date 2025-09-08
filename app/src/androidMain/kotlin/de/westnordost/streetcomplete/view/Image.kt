package de.westnordost.streetcomplete.view

import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.annotation.DrawableRes

/* Same idea here as the Icon class introduced in min API level 23. If the min API level is
   Build.VERSION_CODES_M, usage of this class can be replaced with Icon */

sealed interface Image
data class ResImage(@DrawableRes val resId: Int) : Image
data class DrawableImage(val drawable: Drawable) : Image

fun ImageView.setImage(image: Image?) {
    when (image) {
        is ResImage -> setImageResource(image.resId)
        is DrawableImage -> setImageDrawable(image.drawable)
        null -> setImageDrawable(null)
    }
}
