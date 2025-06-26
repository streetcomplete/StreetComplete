package de.westnordost.streetcomplete.ui.common.input

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.intl.Locale
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
    initialValue: Double?,
    onValueChanged: (Double?) -> Unit,
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
    var text by rememberSaveable(initialValue) {
        mutableStateOf(initialValue?.let { formatter.format(it.absoluteValue) }.orEmpty())
    }

    TextField2(
        value = text,
        onValueChange = { value ->
            if (value.isEmpty()) {
                text = value
                onValueChanged(null)
            }
            else if (value.isOnlyDecimalDigits(
                decimalSeparator = formatter.decimalSeparator,
                maxIntegerDigits = maxIntegerDigits,
                maxFractionDigits = maxFractionDigits
            )) {
                text = value
                val newValue = formatter.parse(value)?.toDouble()
                if (newValue != null) {
                    onValueChanged(newValue)
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
    val format = NumberFormatter()
    Row {
        DecimalInput(null, onValueChanged = { number = it }, Modifier.weight(1f))
        Text(number?.let { format.format(it) }.orEmpty(), Modifier.weight(1f))
    }
}
