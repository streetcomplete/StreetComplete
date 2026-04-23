package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.Length
import androidx.compose.ui.tooling.preview.Preview

/** Input field to input a length in feet+inches */
@Composable
fun LengthFeetInchesInput(
    length: Length.FeetAndInches?,
    onChange: (Length.FeetAndInches?) -> Unit,
    maxFeetDigits: Int,
    modifier: Modifier = Modifier,
    style: TextFieldStyle = TextFieldStyle.Filled,
) {
    var feetState by remember { mutableStateOf(length?.feet) }
    var inchesState by remember { mutableStateOf(length?.inches) }

    // only replace text field content if both foot+inch are specified
    if (length != null) {
        feetState = length.feet
        inchesState = length.inches
    }

    fun callOnChanged() {
        val feet = feetState
        val inches = inchesState
        if (feet != null && inches != null) {
            onChange(Length.FeetAndInches(feet, inches))
        } else {
            // while not both are specified, resulting Length remains null
            onChange(null)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        val feet = feetState?.toString().orEmpty()
        AutoFitTextFieldFontSize(
            value = feet,
            modifier = Modifier.weight(1f)
        ) {
            TextField2(
                value = feet,
                onValueChange = { value ->
                    if (value.isEmpty() || isValidFeetInput(value, maxFeetDigits)) {
                        feetState = value.toIntOrNull()
                        callOnChanged()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = style,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
            )
        }

        Text(
            text = "′",
            fontSize = LocalTextStyle.current.fontSize * 2
        )

        val inches = inchesState?.toString().orEmpty()
        AutoFitTextFieldFontSize(
            value = inches,
            modifier = Modifier.padding(start = 4.dp).weight(1f)
        ) {
            TextField2(
                value = inches,
                onValueChange = { value ->
                    if (value.isEmpty() || isValidInchesInput(value)) {
                        inchesState = value.toIntOrNull()
                        callOnChanged()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = style,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        Text(
            text = "″",
            fontSize = LocalTextStyle.current.fontSize * 2
        )
    }
}

/** Check if the [string] is a valid feet input with a maximum of [maxFeetDigits] digits */
private fun isValidFeetInput(string: String, maxFeetDigits: Int): Boolean {
    if (!string.all { it.isDigit() }) return false
    if (string.length > maxFeetDigits) return false
    return true
}

/** Check if the [string] is a valid inch input */
private fun isValidInchesInput(string: String): Boolean {
    if (!string.all { it.isDigit() }) return false
    val value = string.toInt()
    return value >= 0 && value < 12
}

@Composable @Preview
private fun LengthFeetInchesInputPreview() {
    var feetInch: Length.FeetAndInches? by remember { mutableStateOf(Length.FeetAndInches(3, 11)) }
    ProvideTextStyle(MaterialTheme.typography.body1) {
        LengthFeetInchesInput(
            length = feetInch,
            onChange = { feetInch = it },
            maxFeetDigits = 2,
        )
    }
}
