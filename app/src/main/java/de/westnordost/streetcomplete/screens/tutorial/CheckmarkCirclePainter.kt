package de.westnordost.streetcomplete.screens.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.LeafGreen
import de.westnordost.streetcomplete.ui.util.svgPath

@Composable
fun checkmarkCirclePainter(progress: Float): VectorPainter = rememberVectorPainter(
    defaultWidth = 128.dp,
    defaultHeight = 128.dp,
    viewportWidth = 128f,
    viewportHeight = 128f,
    autoMirror = false
) { _, _ ->
    Path(
        pathData = circlePath,
        strokeLineWidth = 12f,
        stroke = SolidColor(LeafGreen),
        trimPathEnd = (progress * 3f / 2).coerceIn(0f, 1f)
    )
    Path(
        pathData = checkmarkPath,
        strokeLineWidth = 12f,
        stroke = SolidColor(LeafGreen),
        trimPathEnd = ((progress - 2f / 3) * 3f).coerceIn(0f, 1f)
    )
}

private val circlePath = svgPath("m122,64a58,58 0,0 1,-58 58,58 58,0 0,1 -58,-58 58,58 0,0 1,58 -58,58 58,0 0,1 58,58z")
private val checkmarkPath = svgPath("m28.459,67.862c7.344,4.501 19.241,13.97 27.571,23.732 11.064,-20.587 27.756,-39.206 44.333,-55.458")
