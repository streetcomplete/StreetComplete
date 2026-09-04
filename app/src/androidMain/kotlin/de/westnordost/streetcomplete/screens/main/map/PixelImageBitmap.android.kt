package de.westnordost.streetcomplete.screens.main.map

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

internal actual fun IntArray.toPlatformImageBitmap(width: Int, height: Int): ImageBitmap =
    Bitmap.createBitmap(this, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
