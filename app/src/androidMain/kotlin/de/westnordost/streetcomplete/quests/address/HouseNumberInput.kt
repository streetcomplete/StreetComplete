package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.address.parseHouseNumbers
import de.westnordost.streetcomplete.ui.common.SwitchKeyboardPopupButton
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Input field for inputting housenumbers.
 *
 *  Shows buttons to step up the housenumber (12c -> 13 / 12d) and to step down the
 *  housenumber (12c -> 11 / 12b).
 *
 *  When the user didn't input anything yet, will show the [suggestion] for which those buttons
 *  work, too.
 *  */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HouseNumberInput(
    value: String,
    onValueChange: (value: String, usedSuggestion: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: String? = null,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    var isAbc by remember { mutableStateOf(false) }
    val keyboardType = if (isAbc) KeyboardType.Text else KeyboardType.Number

    var isFocused by remember { mutableStateOf(false) }
    val showSwitchKeyboardPopup = isFocused && WindowInsets.isImeVisible

    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    if (value != textFieldValue.text) {
        textFieldValue = textFieldValue.copy(value, TextRange(value.length))
    }
    var textFieldHeightPx by remember { mutableIntStateOf(0) }

    val useSuggestion = textFieldValue.text.isEmpty() && suggestion != null
    val valueOrSuggestion = if (useSuggestion) suggestion else value

    val houseNumbers = remember(valueOrSuggestion) { parseHouseNumbers(valueOrSuggestion) }

    Row(modifier = modifier) {
        val textStyle = textStyle.copy(
            textAlign = TextAlign.Center,
            // to avoid the size of the text changing when going from e.g. "123j" to "123k"
            fontFamily = FontFamily.Monospace,
        )
        TextField2(
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                if (it.text != value) {
                    onValueChange(it.text, false)
                }
            },
            placeholder = if (suggestion != null) { {
                BasicText(
                    text = suggestion,
                    style = textStyle.copy(color = textStyle.color.copy(alpha = 0.2f)),
                    // so that the text aligns center, just like the actual text
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = textStyle.fontSize)
                )
            } } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                autoCorrectEnabled = false,
            ),
            modifier = Modifier
                .weight(1f)
                .onSizeChanged { textFieldHeightPx = it.height }
                .onFocusChanged { isFocused = it.isFocused },
            textStyle = textStyle,
            autoFitFontSize = true,
            singleLine = true,
        )
        val stepperModifier = Modifier.width(48.dp).height(textFieldHeightPx.pxToDp())
        HouseNumberStepperButton(
            value = houseNumbers,
            onValueChange = { onValueChange(it.toString(), useSuggestion) },
            modifier = stepperModifier
        )
        HouseNumberMinorStepperButton(
            value = houseNumbers,
            onValueChange = { onValueChange(it.toString(), useSuggestion) },
            modifier = stepperModifier
        )
        if (showSwitchKeyboardPopup) {
            SwitchKeyboardPopupButton(
                isAbc = isAbc,
                onChange = { isAbc = it },
            )
        }
    }
}

@Composable @Preview
private fun HouseNumberInputPreview() {
    var value by remember { mutableStateOf("") }
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
        HouseNumberInput(
            value = value,
            suggestion = "12c",
            onValueChange = { newValue, usedSuggestion ->
                value = newValue
            },
            modifier = Modifier.width(224.dp)
        )
    }
}
