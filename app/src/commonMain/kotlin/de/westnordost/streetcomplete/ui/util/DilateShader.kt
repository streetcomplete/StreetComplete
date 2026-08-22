package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shader

expect fun createDilateShader(inputImage: ImageBitmap, radius: Float, color: Color): Shader

internal fun createDilateSksl(radius: Float) = """
uniform shader inputImage;
uniform half4 dilationColor;

half4 main(float2 coord) {
    float maxAlpha = 0.0;
    for (float x = -$radius; x <= $radius; x += 1.0) {
        for (float y = -$radius; y <= $radius; y += 1.0) {
            float2 sampleCoord = coord + float2(x, y);
            half4 color = inputImage.eval(sampleCoord);
            maxAlpha = max(maxAlpha, color.a);
        }
    }
    return half4(dilationColor.rgb, dilationColor.a * maxAlpha);
}
"""
