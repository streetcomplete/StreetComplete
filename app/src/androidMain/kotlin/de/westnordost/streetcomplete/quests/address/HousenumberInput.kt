package de.westnordost.streetcomplete.quests.address

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.address.StructuredHouseNumber
import de.westnordost.streetcomplete.osm.address.parseHouseNumbers
import de.westnordost.streetcomplete.ui.common.StepperButton
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
@Composable
fun HousenumberInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    suggestion: String? = null,
    keyboardType: KeyboardType = KeyboardType.Number
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }
    if (value != textFieldValue.text) {
        textFieldValue = textFieldValue.copy(value, TextRange(value.length))
    }
    var textFieldHeightPx by remember { mutableIntStateOf(0) }

    val useSuggestion = textFieldValue.text.isEmpty() && suggestion != null
    val valueOrSuggestion = if (useSuggestion) suggestion else value

    val houseNumbers = remember(valueOrSuggestion) { parseHouseNumbers(valueOrSuggestion) }
    val next = remember(valueOrSuggestion) { houseNumbers?.step(+1) }
    val prev = remember(valueOrSuggestion) { houseNumbers?.step(-1) }
    val nextMinor = remember(valueOrSuggestion) { houseNumbers?.minorStep(+1) }
    val prevMinor = remember(valueOrSuggestion) { houseNumbers?.minorStep(-1) }

    Row(modifier = modifier) {
        StepperButton(
            onIncrease = { if (next != null) onValueChange(next.toString()) },
            onDecrease = { if (prev != null) onValueChange(prev.toString()) },
            modifier = Modifier
                .width(48.dp)
                .height(textFieldHeightPx.pxToDp())
                .alpha(if (next == null && prev == null) 0f else 1f),
            increaseEnabled = next != null,
            decreaseEnabled = prev != null,
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            TextField2(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onValueChange(it.text)
                },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                modifier = Modifier.onSizeChanged { textFieldHeightPx = it.height },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    // to avoid the size of the text changing when going from e.g. "123j" to "123k"
                    fontFamily = FontFamily.Monospace,
                ),
                autoFitFontSize = true,
                singleLine = true,
            )
            if (useSuggestion) {
                BasicText(
                    text = suggestion,
                    modifier = Modifier.padding(16.dp),
                    style = LocalTextStyle.current.copy(
                        color = LocalTextStyle.current.color.copy(alpha = 0.2f),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.Monospace,
                    ),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(maxFontSize = LocalTextStyle.current.fontSize)
                )
            }
        }
        StepperButton(
            onIncrease = { if (nextMinor != null) onValueChange(nextMinor.toString()) },
            onDecrease = { if (prevMinor != null) onValueChange(prevMinor.toString()) },
            modifier = Modifier
                .width(48.dp)
                .height(textFieldHeightPx.pxToDp())
                .alpha(if (nextMinor == null && prevMinor == null) 0f else 1f),
            increaseEnabled = nextMinor != null,
            decreaseEnabled = prevMinor != null,
            increaseContent = { Text(nextMinor?.minor.orEmpty()) },
            decreaseContent = { Text(prevMinor?.minor.orEmpty()) }
        )
    }
}

private val StructuredHouseNumber.minor: String? get() = when (this) {
    is StructuredHouseNumber.Simple -> null
    is StructuredHouseNumber.WithLetter -> letter
    is StructuredHouseNumber.WithNumber -> number2.toString()
}

@Composable @Preview
private fun HousenumberInputPreview() {
    var value by remember { mutableStateOf("") }
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput) {
        HousenumberInput(
            value = value,
            suggestion = "12c",
            onValueChange = { value = it },
            modifier = Modifier.width(224.dp)
        )
    }
}
