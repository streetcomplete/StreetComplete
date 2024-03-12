package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.core.util.TypedValueCompat
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage

inline fun <reified T> Resources.getYamlObject(@RawRes id: Int): T =
    Yaml.default.decodeFromStream(openRawResource(id))

/** shortcut for [getYamlObject] with included type information */
fun Resources.getYamlStringMap(@RawRes id: Int): Map<String, String> = this.getYamlObject(id)

fun Resources.getRawTextFile(@RawRes id: Int) =
    openRawResource(id).bufferedReader().use { it.readText() }

fun Resources.getBitmapDrawable(@DrawableRes id: Int): BitmapDrawable =
    getDrawable(id).asBitmapDrawable(this)

fun Resources.getBitmapDrawable(image: Image): BitmapDrawable =
    getDrawable(image).asBitmapDrawable(this)

fun Resources.getDrawable(image: Image): Drawable = when (image) {
    is ResImage -> getDrawable(image.resId)
    is DrawableImage -> image.drawable
}

/** return the number of density independent pixels for the given pixels */
fun Resources.pxToDp(px: Number): Float = TypedValueCompat.pxToDp(px.toFloat(), displayMetrics)

/** return the number of pixels for the given density independent pixels */
fun Resources.dpToPx(dp: Number): Float = TypedValueCompat.dpToPx(dp.toFloat(), displayMetrics)

/** return the number of pixels for the given scalable pixels */
fun Resources.spToPx(sp: Number): Float = TypedValueCompat.spToPx(sp.toFloat(), displayMetrics)

/** return the number of scalable pixels for the given pixels */
fun Resources.pxToSp(px: Number): Float = TypedValueCompat.pxToSp(px.toFloat(), displayMetrics)
