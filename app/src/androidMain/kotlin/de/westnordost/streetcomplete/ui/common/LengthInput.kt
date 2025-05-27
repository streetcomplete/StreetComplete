package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.util.onlyDecimalDigits
import de.westnordost.streetcomplete.ui.util.validFeetInput
import de.westnordost.streetcomplete.ui.util.validInchInput
import de.westnordost.streetcomplete.view.Text
import kotlin.math.pow

@Composable
fun LengthInput(
    selectedUnit: LengthUnit, currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    modifier: Modifier = Modifier,
    colors: TextFieldColors = TextFieldDefaults.textFieldColors(),
    textStyle: TextStyle = MaterialTheme.typography.h4,
) {
    if (selectedUnit == LengthUnit.METER) {
        LengthInputMeters(
            currentLength,
            syncLength,
            onLengthChanged,
            maxMeterDigits,
            modifier,
            colors,
            textStyle = textStyle
        )
    } else if (selectedUnit == LengthUnit.FOOT_AND_INCH) {
        LengthInputFootInch(
            currentLength,
            syncLength,
            onLengthChanged,
            maxFeetDigits,
            modifier,
            colors,
            textStyle = textStyle
        )
    }
}

@Composable
private fun LengthInputMeters(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxMeterDigits: Pair<Int, Int>,
    modifier: Modifier,
    colors: TextFieldColors,
    textStyle: TextStyle,
) {
    var lengthMeterText by remember { mutableStateOf("") }

    LaunchedEffect(currentLength, syncLength) {
        // Sync from the length only when explicitly allowed (e.g. when inserting data from AR measurement)
        if (syncLength && currentLength is Length.Meters) {
            // Clamp meters to maximum allowed numbers
            val upper = 10.0.pow(maxMeterDigits.first) - 10.0.pow(-maxMeterDigits.second)
            val clamped = currentLength.meters.coerceIn(0.0..upper)
            lengthMeterText = String.format("%.2f", clamped)

            // For the unlikely case, that clamping has happened: Propagate change back to parent
            // so that when still published, the displayed value is used.
            if (clamped != currentLength.meters) {
                onLengthChanged(Length.Meters(clamped))
            }
        }
    }

    TextField(
        value = lengthMeterText,
        onValueChange = { input: String ->
            if (input.isEmpty()) {
                onLengthChanged(null)
                lengthMeterText = input
            } else if (onlyDecimalDigits(
                    input,
                    maxMeterDigits.first,
                    maxMeterDigits.second
                )
            ) {
                onLengthChanged(Length.Meters(input.toDouble()))
                lengthMeterText = input
            }
        },
        textStyle = textStyle.copy(textAlign = TextAlign.Center),
        modifier = modifier.width(130.dp),
        colors = colors,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun LengthInputFootInch(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    modifier: Modifier,
    colors: TextFieldColors,
    textStyle: TextStyle,
) {
    Row(modifier = modifier) {
        var feet by remember {
            mutableStateOf(
                ""
            )
        }
        var inches by remember {
            mutableStateOf(
                ""
            )
        }

        LaunchedEffect(currentLength, syncLength) {
            // Sync from the length only when explicitly allowed (e.g. when inserting data from AR measurement)
            if (syncLength && currentLength is Length.FeetAndInches) {
                // Clamp feet/inches to maximum allowed values
                val upper = 10.0.pow(maxFeetDigits).toInt() - 1
                val clampedFeet = currentLength.feet.coerceIn(0..upper)
                val clampedInch = currentLength.inches.coerceIn(0..11)
                feet = clampedFeet.toString()
                inches = clampedInch.toString()

                // For the unlikely case, that clamping has happened: Propagate change back to parent
                // so that when still published, the displayed value is used.
                if (clampedFeet != currentLength.feet || clampedInch != currentLength.inches) {
                    onLengthChanged(Length.FeetAndInches(clampedFeet, clampedInch))
                }
            }
        }

        TextField(
            value = feet,
            onValueChange = { input: String ->
                if (input.isEmpty()) {
                    onLengthChanged(null)
                    feet = ""
                }
                if (validFeetInput(input, maxFeetDigits)) {
                    val value = input.toInt()
                    if (inches.isNotEmpty()) {
                        onLengthChanged(Length.FeetAndInches(value, inches.toInt()))
                    }
                    feet = value.toString()
                }

            },
            textStyle = textStyle.copy(textAlign = TextAlign.Center),
            modifier = modifier
                .width(95.dp),
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("'", style = textStyle, modifier = Modifier.padding(1.dp))

        TextField(
            value = inches,
            onValueChange = { input: String ->
                if (input.isEmpty()) {
                    onLengthChanged(null)
                    inches = ""
                }
                if (validInchInput(input)) {
                    val value = input.toInt();
                    if (feet.isNotEmpty()) {
                        onLengthChanged(Length.FeetAndInches(feet.toInt(), value))
                    }
                    inches = value.toString()
                }
            },
            textStyle = textStyle.copy(textAlign = TextAlign.Center),
            modifier = Modifier.width(75.dp),
            colors = colors,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text("″", style = textStyle, modifier = Modifier.padding(1.dp))

    }
}

@Composable
@Preview(showBackground = true)
private fun LengthInputPreview() {
    LengthInput(
        selectedUnit = LengthUnit.FOOT_AND_INCH,
        currentLength = Length.FeetAndInches(22, 11),
        syncLength = true,
        onLengthChanged = {},
        maxMeterDigits = Pair(2, 2),
        maxFeetDigits = 2,
    )
}
