package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.util.svgPath
import kotlin.math.PI
import kotlin.math.cos

@Composable
fun mapEditHighlighted(progress: Float): VectorPainter = rememberVectorPainter(
    defaultWidth = 226.dp,
    defaultHeight = 222.dp,
    viewportWidth = 226f,
    viewportHeight = 222f,
    autoMirror = false
) { _, _ ->
    val breathing = -cos(progress * 2 * PI) / 2.0 + 0.5 // 0..1
    Path(
        pathData = wayPath,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineCap = StrokeCap.Round,
        strokeLineWidth = ((breathing + 1) * 8).toFloat(), // 8..16
        stroke = SolidColor(Color(0xffD14000)),
        strokeAlpha = ((1 - breathing) * 0.5 + 0.5).toFloat() // 1 .. 0.5
    )
}

@Composable
fun mapEditDone(progress: Float): VectorPainter = rememberVectorPainter(
    defaultWidth = 226.dp,
    defaultHeight = 222.dp,
    viewportWidth = 226f,
    viewportHeight = 222f,
    autoMirror = false
) { _, _ ->
    Path(
        pathData = wayPath,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineCap = StrokeCap.Round,
        strokeLineWidth = 8 + (1 - progress) * 16,
        stroke = SolidColor(Color(0xff10C1B8)),
        strokeAlpha = progress
    )
}

private val wayPath = svgPath("m53.97,113.51 l-11.99,11.49 0.5,4.5 20.24,24.49 13.99,-6.75 20.49,18.49 -10.49,28.23 10.24,8.5")
