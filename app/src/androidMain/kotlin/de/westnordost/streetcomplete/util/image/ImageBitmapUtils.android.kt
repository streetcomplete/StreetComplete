package de.westnordost.streetcomplete.util.image

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

actual fun loadBitmapFromFile(filePath: String): ImageBitmap? {
    val file = File(filePath)
    if (!file.exists()) return null
    return BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
}
