package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.graphics.Color

fun interpolateColors(color1: Color, color2: Color, progress: Float) = Color(
    red = color1.red * (1 - progress) + color2.red * progress,
    green = color1.green * (1 - progress) + color2.green * progress,
    blue = color1.blue * (1 - progress) + color2.blue * progress,
    alpha = color1.alpha * (1 - progress) + color2.alpha * progress,
)
