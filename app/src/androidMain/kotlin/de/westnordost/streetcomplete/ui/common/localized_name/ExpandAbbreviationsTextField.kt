package de.westnordost.streetcomplete.ui.common.localized_name

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.data.meta.Abbreviations
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.common.colors
import de.westnordost.streetcomplete.ui.common.getContentPadding
import de.westnordost.streetcomplete.ui.common.shape

/** A text field that automatically expands [abbreviations] after completing a word (using the IME
 *  action counts as completing a word). */
@Composable
fun ExpandAbbreviationsTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    abbreviations: Abbreviations?,
    modifier: Modifier = Modifier,
    style: TextFieldStyle = TextFieldStyle.Filled,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = style.shape,
    colors: TextFieldColors = style.colors,
    contentPadding: PaddingValues = style.getContentPadding(label != null),
    autoFitFontSize: Boolean = false,
) {
    TextField2(
        value = value,
        onValueChange = { newValue ->
            val autoExpandedValue =
                if (abbreviations != null && shouldExpandAbbreviation(value, newValue)) {
                    expandAbbreviation(abbreviations, newValue)
                } else {
                    null
                }
            onValueChange(autoExpandedValue ?: newValue)
        },
        modifier = modifier,
        style = style,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions {
            val autoCorrectName = expandAbbreviation(abbreviations, value)
            if (autoCorrectName != null) {
                onValueChange(autoCorrectName)
            } else {
                defaultKeyboardAction(ImeAction.Done)
            }
        },
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        autoFitFontSize = autoFitFontSize,
    )
}

private fun shouldExpandAbbreviation(previousValue: TextFieldValue, newValue: TextFieldValue) =
    newValue.text.length > previousValue.text.length
    && newValue.text[newValue.selection.end - 1] in WORD_END

private fun expandAbbreviation(abbreviations: Abbreviations?, value: TextFieldValue): TextFieldValue? {
    if (abbreviations == null) return null
    val cursor = value.selection.end
    val textUpTillCursor = value.text.substring(0..<cursor).trim()
    val words = textUpTillCursor.split(WORD_BOUNDARY)

    if (words.isEmpty()) return null // e.g. empty string

    val lastWordBeforeCursor = words.last()
    val isFirstWord = words.size == 1
    val isLastWord = cursor == value.text.trimEnd().length

    val replacement = abbreviations.getExpansion(lastWordBeforeCursor, isFirstWord, isLastWord)
    if (replacement == null) return null // no abbreviation to expand found

    val wordStart = value.text.indexOf(lastWordBeforeCursor)
    val wordEnd = wordStart + lastWordBeforeCursor.length
    return value.copy(
        text = value.text.substring(0..<wordStart) + replacement + value.text.substring(wordEnd),
        selection = TextRange(wordStart + replacement.length + 1),
    )
}

private val WORD_END = setOf(' ','-','.','\n')
private val WORD_BOUNDARY = Regex("[\n -]+")

@Preview @Composable
private fun ExpandAbbreviationsTextFieldPreview() {
    var value by remember { mutableStateOf(TextFieldValue()) }
    ExpandAbbreviationsTextField(
        value = value,
        onValueChange = {
            value = it
        },
        abbreviations = Abbreviations(mapOf(
            "...str?$" to "straße",
            "Prof" to "Professor",
        )),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
    )
}
