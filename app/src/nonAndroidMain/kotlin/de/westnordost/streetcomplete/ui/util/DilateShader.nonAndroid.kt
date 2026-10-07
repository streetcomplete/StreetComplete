package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.asComposeShader
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

actual fun createDilateShader(inputImage: ImageBitmap, radius: Float, color: Color): Shader {
    val effect = RuntimeEffect.makeForShader(createDilateSksl(radius))
    val shaderBuilder = RuntimeShaderBuilder(effect).apply {
        child("inputImage", inputImage.asSkiaBitmap().makeShader())
        uniform("dilationColor", color.red, color.green, color.blue, color.alpha)
    }
    return shaderBuilder.makeShader().asComposeShader()
}
