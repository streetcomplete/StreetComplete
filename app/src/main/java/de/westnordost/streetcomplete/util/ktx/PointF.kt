package de.westnordost.streetcomplete.util.ktx

import android.graphics.PointF
import kotlin.math.cos
import kotlin.math.sin

fun PointF.translate(distance: Float, angle: Double) =
    PointF((x + distance * cos(angle)).toFloat(), (y + distance * sin(angle)).toFloat())
