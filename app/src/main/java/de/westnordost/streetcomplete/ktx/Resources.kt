package de.westnordost.streetcomplete.ktx

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import com.esotericsoftware.yamlbeans.YamlReader
import java.io.BufferedReader
import java.io.InputStreamReader

inline fun <reified T> Resources.getYamlObject(@RawRes id: Int): T =
    YamlReader(BufferedReader(InputStreamReader(openRawResource(id)))).read(T::class.java)

fun Resources.getBitmapDrawable(@DrawableRes id: Int): BitmapDrawable {
    val d = getDrawable(id)
    return if (d is BitmapDrawable) d else BitmapDrawable(this, d.createBitmap())
}
