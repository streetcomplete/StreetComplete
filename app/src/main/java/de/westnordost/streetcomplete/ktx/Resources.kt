package de.westnordost.streetcomplete.ktx

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.charleskorn.kaml.Yaml
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

inline fun <reified T> Resources.getYamlObject(serializer: KSerializer<T>, @RawRes id: Int): T =
    Yaml.default.decodeFromStream(serializer, openRawResource(id))

fun Resources.getYamlStringMap(@RawRes id: Int): Map<String, String> =
    this.getYamlObject(MapSerializer(String.serializer(), String.serializer()), id)

fun Resources.getYamlStringList(@RawRes id: Int): List<String> =
    this.getYamlObject(ListSerializer(String.serializer()), id)

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
