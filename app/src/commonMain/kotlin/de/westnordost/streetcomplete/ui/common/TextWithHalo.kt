package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.material.LocalElevationOverlay
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.toPx

/* It would have been cleaner to have this as kind of an extension to TextStyle, a custom modifier
   or something. But either neither of the two is possible or I am not experienced enough in Compose
   to devise that. */

/** A text that has a halo. */
@Composable
fun TextWithHalo(
    text: String,
    modifier: Modifier = Modifier,
    haloColor: Color = MaterialTheme.colors.surface,
    haloWidth: Dp = 2.dp,
    elevation: Dp = 0.dp,
    color: Color = contentColorFor(haloColor),
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current
) {
    val haloColor2 = LocalElevationOverlay.current?.apply(haloColor, elevation) ?: haloColor
    // * 2 because the stroke is painted half outside and half inside of the text shape
    val stroke = Stroke(haloWidth.toPx() * 2, cap = StrokeCap.Round, join = StrokeJoin.Round)
    Box {
        Text(
            text, modifier, haloColor2, fontSize, fontStyle, fontWeight, fontFamily,
            letterSpacing, textDecoration, textAlign, lineHeight, overflow, softWrap, maxLines,
            minLines, onTextLayout, style.copy(drawStyle = stroke)
        )
        Text(
            text, modifier, color, fontSize, fontStyle, fontWeight, fontFamily, letterSpacing,
            textDecoration, textAlign, lineHeight, overflow, softWrap, maxLines, minLines,
            null, style
        )
    }
}
