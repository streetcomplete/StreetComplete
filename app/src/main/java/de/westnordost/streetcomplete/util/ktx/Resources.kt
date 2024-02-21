package de.westnordost.streetcomplete.util.ktx

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

inline fun <reified T> Resources.getYamlObject(@RawRes id: Int): T =
    Yaml.default.decodeFromStream(openRawResource(id))

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Resources.getJsonObject(@RawRes id: Int): T =
    Json.decodeFromStream(openRawResource(id))

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
