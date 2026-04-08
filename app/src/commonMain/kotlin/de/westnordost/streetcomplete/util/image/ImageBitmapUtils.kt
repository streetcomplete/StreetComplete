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
import kotlinx.io.buffered
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import org.jetbrains.compose.resources.decodeToImageBitmap

@Composable
fun fileBitmapPainter(fileSystem: FileSystem, file: Path): Painter? {
    val imageBitmap by produceState<ImageBitmap?>(initialValue = null, key1 = file) {
        value = withContext(Dispatchers.IO) { fileSystem.loadImageBitmap(file) }
    }
    return remember(imageBitmap) { imageBitmap?.let { BitmapPainter(it) } }
}

fun FileSystem.loadImageBitmap(path: Path): ImageBitmap? = try {
    if (exists(path)) {
        source(path).buffered().use { it.readByteArray() }.decodeToImageBitmap()
    } else {
        null
    }
} catch (e: Exception) {
    null
}
