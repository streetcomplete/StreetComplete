package de.westnordost.streetcomplete.ui.util

import android.graphics.BitmapShader
import android.graphics.Shader.TileMode.CLAMP
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asAndroidBitmap

actual fun createDilateShader(inputImage: ImageBitmap, radius: Float, color: Color): Shader {
    val bitmapShader = BitmapShader(inputImage.asAndroidBitmap(), CLAMP, CLAMP)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val dilateShader = RuntimeShader(createDilateSksl(radius))
        dilateShader.setInputShader("inputImage", bitmapShader)
        dilateShader.setFloatUniform("dilationColor", color.red, color.green, color.blue, color.alpha)
        dilateShader
    } else {
        bitmapShader
    }
}
