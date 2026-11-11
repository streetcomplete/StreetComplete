package de.westnordost.streetcomplete.ui.util

import android.graphics.Bitmap as AndroidBitmap
import android.graphics.BitmapShader
import android.graphics.Color.alpha
import android.graphics.Color.red
import android.graphics.Color.green
import android.graphics.Color.blue
import android.graphics.Color.argb
import android.graphics.RuntimeShader
import android.graphics.Shader.TileMode.CLAMP
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max

actual fun createDilateShader(inputImage: ImageBitmap, radius: Float, color: Color): Shader {
    val bitmap = inputImage.asAndroidBitmap()

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val dilateShader = RuntimeShader(createDilateSksl(radius))
        dilateShader.setInputShader("inputImage", BitmapShader(bitmap, CLAMP, CLAMP))
        dilateShader.setFloatUniform("dilationColor", color.red, color.green, color.blue, color.alpha)
        dilateShader
    } else {
        // hm, runtime shaders are not available. But we have the android bitmap. So, let's just
        // do what the shader would do on the bitmap directly (on the CPU)
        val dilated = bitmap.dilated(radius.toInt(), color)
        bitmap.recycle()
        BitmapShader(dilated, CLAMP, CLAMP)
    }
}

private fun AndroidBitmap.dilated(radius: Int, color: Color): AndroidBitmap {
    val pixels = IntArray(width * height)
    getPixels(pixels, 0, width, 0, 0, width, height)

    val argb = color.toArgb()
    val (a, r, g, b) = arrayOf(alpha(argb), red(argb), green(argb), blue(argb))

    // for each pixel...
    for (i in pixels.indices) {
        val px = i % width
        val py = i / width

        var maxAlpha = 0
        // in a radius around that pixel...
        perPixel@ for (dx in -radius..radius) {
            val x = px + dx
            if (x !in 0..<width) continue
            for (dy in -radius..radius) {
                val y = py + dy
                if (y !in 0..<height) continue

                // determine the alpha of the pixel with the highest alpha
                maxAlpha = max(maxAlpha, alpha(getPixel(x, y)))
                // early return: there is already a pixel with max alpha
                if (maxAlpha == 255) break@perPixel
            }
        }

        pixels[i] = argb(maxAlpha * a / 255, r, g, b)
    }
    // copy into new bitmap
    return AndroidBitmap.createBitmap(pixels, width, height, AndroidBitmap.Config.ARGB_8888)
}
