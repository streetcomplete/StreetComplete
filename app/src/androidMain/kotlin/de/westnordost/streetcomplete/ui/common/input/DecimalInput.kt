package de.westnordost.streetcomplete.ui.common.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.TextFieldStyle
import de.westnordost.streetcomplete.ui.common.colors
import de.westnordost.streetcomplete.ui.common.getContentPadding
import de.westnordost.streetcomplete.ui.common.shape
import de.westnordost.streetcomplete.util.locale.NumberFormatter
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.absoluteValue

/**
 * Input single-line [TextField][androidx.compose.material.TextField] for unsigned decimal numbers.
 *
 * Specify [maxIntegerDigits] and [maxFractionDigits] to limit the maximum input length.
 * */
@Composable
fun DecimalInput(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    maxIntegerDigits: Int = Int.MAX_VALUE,
    maxFractionDigits: Int = Int.MAX_VALUE,
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
    keyboardActions: KeyboardActions = KeyboardActions(),
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = style.shape,
    colors: TextFieldColors = style.colors,
    contentPadding: PaddingValues = style.getContentPadding(label != null),
    autoFitFontSize: Boolean = false,
) {
    val locale = Locale.current
    val formatter = remember(locale, maxIntegerDigits, maxFractionDigits) {
        NumberFormatter(
            locale = locale,
            maxIntegerDigits = maxIntegerDigits,
            maxFractionDigits = maxFractionDigits
        )
    }
    // number value as text
    val textValue = value?.let { formatter.format(it.absoluteValue) }.orEmpty()

    // initialize the text state once, then on number text change, change only the text (not the
    // caret position etc.).
    // The following logic is pretty much copied from BasicTextField(value: String, … )
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(textValue)) }

    // only replace text if passed value is actually a different decimal from the current text
    // field content.
    // Don't replace e.g. "0." with "0" because then when typing, a decimal separator would
    // immediately be replaced with it being removed, lol
    val valueDiffers = formatter.parse(textFieldValueState.text)?.toDouble() != value
    if (valueDiffers) {
        textFieldValueState = textFieldValueState.copy(text = textValue)
    }

    // remember last value so to only call onValueChanged if it actually did change (also copied
    // from BasicTextField)
    var lastValue by remember(value) { mutableStateOf(value) }

    TextField2(
        value = textFieldValueState,
        onValueChange = { newTextFieldValueState ->
            // cleared input -> value now null
            if (newTextFieldValueState.text.isEmpty() && lastValue != null) {
                textFieldValueState = newTextFieldValueState
                lastValue = null
                onValueChange(null)
            }
            // accept only decimal input
            else if (newTextFieldValueState.text.isOnlyDecimalDigits(
                decimalSeparator = formatter.decimalSeparator,
                maxIntegerDigits = maxIntegerDigits,
                maxFractionDigits = maxFractionDigits
            )) {
                textFieldValueState = newTextFieldValueState

                val newValue = formatter.parse(newTextFieldValueState.text)?.toDouble()
                if (newValue != null && lastValue != newValue) {
                    lastValue = newValue
                    onValueChange(newValue)
                }
            }
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
        keyboardOptions = keyboardOptions.copy(keyboardType = KeyboardType.Decimal),
        keyboardActions = keyboardActions,
        singleLine = true,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        contentPadding = contentPadding,
        autoFitFontSize = autoFitFontSize
    )
}

/** Checks if string has at most one decimal separator, otherwise only consists of digits and has
 *  at most the given number of digits */
private fun String.isOnlyDecimalDigits(
    decimalSeparator: Char,
    maxIntegerDigits: Int,
    maxFractionDigits: Int
): Boolean {
    if (!all { it.isDigit() || it == decimalSeparator }) return false
    val texts = split(decimalSeparator)
    if (texts.size > 2 || texts.isEmpty()) return false
    if (texts[0].length > maxIntegerDigits) return false
    if (texts.size > 1 && texts[1].length > maxFractionDigits) return false
    return true
}

@Preview @Composable
private fun DecimalInputPreview() {
    var number: Double? by remember { mutableStateOf(null) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        DecimalInput(
            value = number,
            onValueChange = { number = it },
            maxIntegerDigits = 2,
            maxFractionDigits = 2,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(80.dp),
        )
        Text(number?.toString().orEmpty(), Modifier.padding(16.dp))
    }
}
