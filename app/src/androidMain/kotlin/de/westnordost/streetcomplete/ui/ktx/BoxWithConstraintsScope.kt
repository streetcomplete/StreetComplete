package de.westnordost.streetcomplete.ui.ktx

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun BoxWithConstraintsScope.calculateTextMaxFontSize(
    text: String,
    textStyle: TextStyle,
    minFontSize: TextUnit = 10.sp,
    contentPadding: PaddingValues = PaddingValues(),
    maxLines: Int = 1,
): TextUnit {
    val ltr = LayoutDirection.Ltr
    val hPad = contentPadding.calculateLeftPadding(ltr) + contentPadding.calculateRightPadding(ltr)
    var fontSize = textStyle.fontSize.takeIf { it.isSpecified } ?: MaterialTheme.typography.body1.fontSize
    val calculateParagraph = @Composable {
        Paragraph(
            text = text,
            style = textStyle.copy(fontSize = fontSize),
            constraints = Constraints(maxWidth = this.maxWidth.toPx().roundToInt()),
            density = LocalDensity.current,
            fontFamilyResolver = LocalFontFamilyResolver.current,
            maxLines = maxLines,
        )
    }
    var paragraph = calculateParagraph()
    while (
        fontSize >= minFontSize && (
            paragraph.didExceedMaxLines ||
            paragraph.maxIntrinsicWidth > (this.maxWidth - hPad).toPx()
        )
    ) {
        fontSize *= 0.9f
        paragraph = calculateParagraph()
    }
    return fontSize
}
