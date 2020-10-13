package de.westnordost.streetcomplete.view.image_select

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

interface DisplayItem<T> {
    val value: T?
    val image: Image?
    val title: Text?
    val description: Text?
}

interface GroupableDisplayItem<T> : DisplayItem<T> {
    val items: List<GroupableDisplayItem<T>>?
    val isGroup: Boolean get() = !items.isNullOrEmpty()
}


sealed class Text
data class ResText(@StringRes val resId: Int) : Text()
data class CharSequenceText(val text: CharSequence) : Text()

fun TextView.setText(text: Text?) {
    when(text) {
        is ResText -> setText(text.resId)
        is CharSequenceText -> setText(text.text)
        null -> setText("")
    }
}

sealed class Image
data class ResImage(@DrawableRes val resId: Int) : Image()
data class DrawableImage(val drawable: Drawable) : Image()
data class BitmapImage(val bitmap: Bitmap) : Image()
data class URIImage(val uri: Uri) : Image()

fun ImageView.setImage(image: Image?) {
    when(image) {
        is ResImage -> setImageResource(image.resId)
        is DrawableImage -> setImageDrawable(image.drawable)
        is BitmapImage -> setImageBitmap(image.bitmap)
        is URIImage -> setImageURI(image.uri)
        null -> setImageDrawable(null)
    }
}