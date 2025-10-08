package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import de.westnordost.streetcomplete.ui.common.AutoFitTextFieldFontSize
import de.westnordost.streetcomplete.ui.common.SwitchKeyboardPopupButton
import de.westnordost.streetcomplete.ui.common.TextField2

/** An input field for adding some address number. There's something all these fields have in
 *  common, which is that
 *  - they are single-line text fields with auto-size on
 *  - one can switch between text and number software keyboard
 *  - a suggestion can be displayed that is sized the same as the actual input, only with less alpha
 *  - certain common text styling (monospace, centered) */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnAddressNumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var valueState by remember { mutableStateOf(TextFieldValue(text = value)) }
    if (value != valueState.text) {
        // on incoming value, place cursor at end so that user can directly delete it again with ⌫
        valueState = valueState.copy(value, TextRange(value.length))
    }

    var isAbc by remember { mutableStateOf(false) }
    val keyboardType = if (isAbc) KeyboardType.Text else KeyboardType.Number

    var isFocused by remember { mutableStateOf(false) }
    val showSwitchKeyboardPopup = isFocused && WindowInsets.isImeVisible

    ProvideTextStyle(LocalTextStyle.current.copy(
        textAlign = TextAlign.Center,
        // to avoid the size of the text changing when going from e.g. "123j" to "123k"
        fontFamily = FontFamily.Monospace,
    )) {
        val textStyle = LocalTextStyle.current
        AutoFitTextFieldFontSize(
            value = valueState.text,
            modifier = modifier
        ) {
            TextField2(
                value = valueState,
                onValueChange = {
                    valueState = it
                    onValueChange(valueState.text)
                },
                placeholder = if (!suggestion.isNullOrEmpty()) { {

                    BasicText(
                        text = suggestion,
                        style = textStyle.copy(color = textStyle.color.copy(alpha = 0.2f)),
                        // so that the text aligns center, just like the actual text
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        autoSize = TextAutoSize.StepBased(maxFontSize = textStyle.fontSize)
                    )
                } } else null,
                keyboardOptions = keyboardOptions.copy(
                    keyboardType = keyboardType,
                    autoCorrectEnabled = false,
                ),
                modifier = Modifier.onFocusChanged { isFocused = it.isFocused },
                singleLine = true,
            )
        }

        if (showSwitchKeyboardPopup) {
            SwitchKeyboardPopupButton(
                isAbc = isAbc,
                onChange = { isAbc = it },
            )
        }
    }
}
