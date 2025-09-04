package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.Group
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.VectorComposable
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.util.svgPath
import kotlin.math.PI
import kotlin.math.cos

@Composable
fun overlayPainter(progress: Float): VectorPainter = rememberMapOverlayPainter {
    val translate = -(1f - progress.coerceIn(0f, 1f)) * 317f
    Group(
        clipPathData = clipPath,
        translationX = translate,
        translationY = translate,
    ) {
        Group(
            translationX = -translate,
            translationY = -translate,
        ) {
            Path(
                pathData = way1Path,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineCap = StrokeCap.Round,
                strokeLineWidth = 8f,
                stroke = SolidColor(Color(0xffeebd0d))
            )
            Path(
                pathData = way2Path,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineCap = StrokeCap.Round,
                strokeLineWidth = 8f,
                stroke = SolidColor(Color(0xffff0000))
            )
            Path(
                pathData = way3Path,
                strokeLineJoin = StrokeJoin.Round,
                strokeLineCap = StrokeCap.Round,
                strokeLineWidth = 8f,
                stroke = SolidColor(Color(0xff1a87e6))
            )
        }
    }
}

@Composable
fun overlayEditHighlightedPainter(progress: Float): VectorPainter = rememberMapOverlayPainter {
    val breathing = -cos(progress * 2 * PI) / 2.0 + 0.5 // 0..1
    Path(
        pathData = way2Path,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineCap = StrokeCap.Round,
        strokeLineWidth = ((breathing + 1) * 8).toFloat(), // 8..16
        stroke = SolidColor(Color(0xffD14000)),
        strokeAlpha = ((1 - breathing) * 0.5 + 0.5).toFloat() // 1 .. 0.5
    )
}

@Composable
fun overlayEditDonePainter(progress: Float): VectorPainter = rememberMapOverlayPainter {
    Path(
        pathData = way2Path,
        strokeLineJoin = StrokeJoin.Round,
        strokeLineCap = StrokeCap.Round,
        strokeLineWidth = 8 + (1 - progress.coerceIn(0f, 1f)) * 16,
        stroke = SolidColor(Color(0xff10C1B8)),
        strokeAlpha = progress.coerceIn(0f, 1f)
    )
}

@Composable
private fun rememberMapOverlayPainter(content: @Composable @VectorComposable () -> Unit) =
    rememberVectorPainter(
        defaultWidth = 226.dp,
        defaultHeight = 222.dp,
        viewportWidth = 226f,
        viewportHeight = 222f,
        autoMirror = false
    ) { _, _ -> content() }

private val clipPath = svgPath("M-111,111 l317,317 l317,-317 l-317,-317z")
private val way1Path = svgPath("m48.72,10.05 l-8.5,28.23 17.99,6.25 7.75,36.23 -20.99,22.24 8.99,10.49")
private val way2Path = svgPath("m53.97,113.51 l-11.99,11.49 0.5,4.5 20.24,24.49 13.99,-6.75 20.49,18.49 -10.49,28.23 10.24,8.5")
private val way3Path = svgPath("M94.19,215.44 l2.5,-13.24 l12.49,-27.73 10.99,-7 27.48,15.74 20.49,-3.75 -0.25,-15.74 -10.24,-6 12.74,-26.23 5.75,-3.75 38.73,-9.99")
