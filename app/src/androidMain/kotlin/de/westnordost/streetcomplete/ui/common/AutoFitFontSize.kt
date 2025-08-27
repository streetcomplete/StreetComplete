package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.calculateTextMaxFontSize

/** Make the font size for any child composables scale to fit the given size. Usually you want to
 *  use [AutoFitTextFieldFontSize] with [TextField2]. */
@Composable
fun AutoFitFontSize(
    value: String,
    modifier: Modifier = Modifier,
    maxLines: Int = 1,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val textStyle = LocalTextStyle.current
    BoxWithConstraints(modifier) {
        val fontSize = calculateTextMaxFontSize(
            text = value,
            textStyle = textStyle,
            contentPadding = contentPadding,
            maxLines = maxLines
        )
        ProvideTextStyle(textStyle.copy(fontSize = fontSize)) {
            content()
        }
    }
}

/** Make the font size for any child composables scale to fit the given size for text fields */
@Composable
fun AutoFitTextFieldFontSize(
    value: String,
    modifier: Modifier = Modifier,
    style: TextFieldStyle = TextFieldStyle.Filled,
    hasLabel: Boolean = false,
    maxLines: Int = 1,
    contentPadding: PaddingValues = style.getContentPadding(hasLabel),
    content: @Composable () -> Unit,
) {
    AutoFitFontSize(
        value = value,
        modifier = modifier,
        maxLines = maxLines,
        contentPadding = contentPadding,
        content = content,
    )
}
