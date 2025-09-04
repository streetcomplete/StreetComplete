package de.westnordost.streetcomplete.screens.main.controls.ktx

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultBlendMode
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.drawText

/** Draw several lines. Each offset in [path] is relative to the previous. */
internal fun DrawScope.drawPath(
    color: Color,
    path: List<Offset>,
    strokeWidth: Float = Stroke.HairlineWidth,
    cap: StrokeCap = Stroke.DefaultCap,
    pathEffect: PathEffect? = null,
    alpha: Float = 1.0f,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DefaultBlendMode,
) {
    val it = path.iterator()
    if (!it.hasNext()) return
    var start = it.next()
    while (it.hasNext()) {
        val end = start + it.next()
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = cap,
            pathEffect = pathEffect,
            alpha = alpha,
            colorFilter = colorFilter,
            blendMode = blendMode,
        )
        start = end
    }
}

/** Draw several paths with halo. All halos of all [paths] are behind all strokes. */
internal fun DrawScope.drawPathsWithHalo(
    color: Color,
    haloColor: Color,
    paths: List<List<Offset>>,
    strokeWidth: Float = Stroke.HairlineWidth,
    haloWidth: Float = Stroke.HairlineWidth,
    cap: StrokeCap = Stroke.DefaultCap,
) {
    if (haloWidth > 0f && haloColor != Color.Transparent) {
        for (path in paths) {
            drawPath(color = haloColor, path = path, strokeWidth = strokeWidth + haloWidth * 2, cap = cap)
        }
    }
    for (path in paths) {
        drawPath(color = color, path = path, strokeWidth = strokeWidth, cap = cap)
    }
}

internal fun DrawScope.drawTextWithHalo(
    textLayoutResult: TextLayoutResult,
    topLeft: Offset = Offset.Zero,
    color: Color = Color.Unspecified,
    haloColor: Color = Color.Unspecified,
    haloWidth: Float = 0f,
) {
    // * 2 because the stroke is painted half outside and half inside of the text shape
    if (haloWidth > 0f && haloColor != Color.Transparent) {
        // * 2 because the stroke is painted half outside and half inside of the text shape
        val stroke = Stroke(width = haloWidth * 2, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawText(
            textLayoutResult = textLayoutResult,
            color = haloColor,
            topLeft = topLeft,
            drawStyle = stroke,
        )
    }
    drawText(textLayoutResult = textLayoutResult, color = color, topLeft = topLeft, drawStyle = Fill)
}
