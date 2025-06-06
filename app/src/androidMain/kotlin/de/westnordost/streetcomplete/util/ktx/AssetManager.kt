package de.westnordost.streetcomplete.util.ktx

import android.content.res.AssetManager
import java.io.IOException
import java.io.InputStream

fun AssetManager.openOrNull(filename: String): InputStream? =
    try { open(filename) } catch (_: IOException) { null }
