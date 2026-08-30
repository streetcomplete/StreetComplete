package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun Offset.translate(distance: Float, angle: Double) =
    Offset((x + distance * cos(angle)).toFloat(), (y + distance * sin(angle)).toFloat())

fun Offset.length() =
    sqrt(x * x + y * y)

fun Offset.toIntOffset() = IntOffset(x.toInt(), y.toInt())
