package de.westnordost.streetcomplete.ui.util

import android.graphics.BitmapShader
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.nativeCanvas

actual fun createDilateShader(inputImage: ImageBitmap, radius: Float, color: Color, canvas: Canvas): Shader? {
    // TODO maplibre-compose: enable hardware rasterization of map images to support shader halos.
    // Also support halos below API 33, where RuntimeShader is unavailable.
    if (!canvas.nativeCanvas.isHardwareAccelerated || Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return null
    }
    val bitmapShader = BitmapShader(inputImage.asAndroidBitmap(), CLAMP, CLAMP)
    val dilateShader = RuntimeShader(createDilateSksl(radius))
    dilateShader.setInputShader("inputImage", bitmapShader)
    dilateShader.setFloatUniform("dilationColor", color.red, color.green, color.blue, color.alpha)
    return dilateShader
}
