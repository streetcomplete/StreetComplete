package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.calculateTextMaxFontSize

/** Changes the font size of the `LocalTextStyle` so that the given [text] fits within the given
 *  constraints. */
@Composable
fun AutoFit(
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    // 2*16.dp is the default (and unchangable) material inner text field padding
    horizontalContentPadding: Dp = 32.dp,
    maxLines: Int = 1,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier) {
        val fontSize = calculateTextMaxFontSize(text, textStyle, horizontalContentPadding, maxLines)
        CompositionLocalProvider(LocalTextStyle provides textStyle.copy(fontSize = fontSize)) {
            content()
        }
    }
}
