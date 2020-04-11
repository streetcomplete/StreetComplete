package de.westnordost.streetcomplete.ktx

import android.content.res.Resources
import androidx.annotation.RawRes
import com.esotericsoftware.yamlbeans.YamlReader
import java.io.BufferedReader
import java.io.InputStreamReader

inline fun <reified T> Resources.getYamlObject(@RawRes id: Int): T =
    YamlReader(BufferedReader(InputStreamReader(openRawResource(id)))).read(T::class.java)
