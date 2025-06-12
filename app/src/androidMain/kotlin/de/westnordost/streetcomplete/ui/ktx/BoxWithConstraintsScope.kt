package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun BoxWithConstraintsScope.calculateTextFieldMaxFontSize(
    text: String,
    textStyle: TextStyle
) : TextUnit =
    // 2*16.dp is the default (and unchangable) material inner text field padding
    calculateTextMaxFontSize(text, textStyle, 32.dp)

@Composable
fun BoxWithConstraintsScope.calculateTextMaxFontSize(
    text: String,
    textStyle: TextStyle,
    horizontalContentPadding: Dp = 0.dp,
): TextUnit {
    var fontSize = textStyle.fontSize
    val calculateParagraph = @Composable {
        Paragraph(
            text = text,
            constraints = Constraints(maxWidth = this.maxWidth.toPx().roundToInt()),
            style = textStyle.copy(fontSize = fontSize),
            density = LocalDensity.current,
            fontFamilyResolver = LocalFontFamilyResolver.current,
        )
    }
    var paragraph = calculateParagraph()
    while (paragraph.maxIntrinsicWidth > (maxWidth - horizontalContentPadding).toPx()) {
        fontSize *= 0.9f
        paragraph = calculateParagraph()
    }
    return fontSize
}
