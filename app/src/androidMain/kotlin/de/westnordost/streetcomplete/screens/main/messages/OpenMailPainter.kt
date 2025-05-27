package de.westnordost.streetcomplete.screens.main.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.util.interpolateColors

@Composable
fun openMailPainter(progress: Float): VectorPainter = rememberVectorPainter(
    defaultWidth = 288.dp,
    defaultHeight = 288.dp,
    viewportWidth = 288f,
    viewportHeight = 288f,
    autoMirror = false
) { _, _ ->
    Path(
        pathData = PathData {
            moveTo(7.911f, 121.751f)
            lineTo(142.5f, 238.62f * (1f - progress) + 6f)
            lineTo(275.119f, 121.751f)
            close()
        },
        strokeLineJoin = StrokeJoin.Round,
        strokeLineCap = StrokeCap.Round,
        strokeLineWidth = 6f,
        stroke = SolidColor(Color(0xffD3BF95)),
        fill = SolidColor(interpolateColors(Color(0xffFFF7E0), Color(0xffD3BF95), progress)),
    )
}
