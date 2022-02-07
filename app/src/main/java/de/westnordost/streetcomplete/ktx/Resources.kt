package de.westnordost.streetcomplete.ktx

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.esotericsoftware.yamlbeans.YamlReader
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import java.io.BufferedReader
import java.io.InputStreamReader

inline fun <reified T> Resources.getYamlObject(@RawRes id: Int): T =
    YamlReader(BufferedReader(InputStreamReader(openRawResource(id)))).read(T::class.java)

fun Resources.getBitmapDrawable(@DrawableRes id: Int): BitmapDrawable =
    getDrawable(id).asBitmapDrawable(this)

fun Resources.getBitmapDrawable(image: Image): BitmapDrawable =
    getDrawable(image).asBitmapDrawable(this)

fun Resources.getDrawable(image: Image): Drawable = when (image) {
    is ResImage -> getDrawable(image.resId)
    is DrawableImage -> image.drawable
}

fun Resources.updateConfiguration(block: Configuration.() -> Unit) =
    updateConfiguration(Configuration(configuration).apply(block), displayMetrics)
