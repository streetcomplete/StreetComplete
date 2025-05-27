package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.toPx

/** A counter that has a halo. */
@Composable
fun CounterWithHalo(
    count: Int,
    modifier: Modifier = Modifier,
    haloColor: Color = MaterialTheme.colors.surface,
    haloWidth: Dp = 2.dp,
    elevation: Dp = 0.dp,
    clip: Boolean = false,
    color: Color = contentColorFor(haloColor),
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val haloColor2 = LocalElevationOverlay.current?.apply(haloColor, elevation) ?: haloColor
    val stroke = Stroke(haloWidth.toPx() * 2, cap = StrokeCap.Round, join = StrokeJoin.Round)
    Box {
        Counter(
            count, modifier, clip, haloColor2, fontSize, fontStyle, fontWeight, fontFamily,
            letterSpacing, textDecoration, textAlign, lineHeight, onTextLayout,
            style.copy(drawStyle = stroke)
        )
        Counter(
            count, modifier, clip, color, fontSize, fontStyle, fontWeight, fontFamily,
            letterSpacing, textDecoration, textAlign, lineHeight, null, style
        )
    }
}
