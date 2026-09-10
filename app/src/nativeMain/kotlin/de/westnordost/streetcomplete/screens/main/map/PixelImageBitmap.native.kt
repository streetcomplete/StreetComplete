package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ColorSpace
import org.jetbrains.skia.ColorType
import org.jetbrains.skia.ImageInfo

internal actual fun IntArray.toPlatformImageBitmap(width: Int, height: Int): ImageBitmap =
    toSkiaImageBitmap(width, height)

private fun IntArray.toSkiaImageBitmap(width: Int, height: Int): ImageBitmap {
    val bitmap = Bitmap()
    val info = ImageInfo(
        width,
        height,
        ColorType.RGBA_8888,
        ColorAlphaType.UNPREMUL,
        ColorSpace.sRGB,
    )
    val bytes = ByteArray(width * height * info.bytesPerPixel)
    forEachIndexed { index, pixel ->
        bytes[index * 4] = (pixel shr 16).toByte()
        bytes[index * 4 + 1] = (pixel shr 8).toByte()
        bytes[index * 4 + 2] = pixel.toByte()
        bytes[index * 4 + 3] = (pixel shr 24).toByte()
    }
    bitmap.installPixels(info, bytes, info.minRowBytes)
    return bitmap.asComposeImageBitmap()
}
