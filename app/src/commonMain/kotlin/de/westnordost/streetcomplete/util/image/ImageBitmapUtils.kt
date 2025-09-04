package de.westnordost.streetcomplete.util.image

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

expect fun loadBitmapFromFile(filePath: String): ImageBitmap?

@Composable
fun fileBitmapPainter(filePath: String): Painter? {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = filePath) {
        value = withContext(Dispatchers.IO) { loadBitmapFromFile(filePath) }
    }
    return remember(imageBitmap) { imageBitmap?.let { BitmapPainter(it) } }
}
