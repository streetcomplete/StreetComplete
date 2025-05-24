package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters
import de.westnordost.streetcomplete.ui.common.MeasurementIcon
import kotlin.math.pow

@Composable
fun LengthForm(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    maxMeterDigits: Pair<Int, Int>,
    selectableUnits: List<LengthUnit>,
    onUnitChanged: (LengthUnit) -> Unit,
    showMeasureButton: Boolean,
    takeMeasurementClick: () -> Unit,
    explanation: String?,
    modifier: Modifier = Modifier,
) {
    val selectedUnitIndex = remember { mutableIntStateOf(0) }

    // Trigger unit changed event to sync with parent
    onUnitChanged(selectableUnits[selectedUnitIndex.intValue])

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        if (explanation != null) {
            Text(
                text = explanation,
                modifier = Modifier
                    .wrapContentHeight()
            )
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        )

        {
            if (selectableUnits[selectedUnitIndex.intValue] == LengthUnit.METER) {
                LengthInputMeters(currentLength, syncLength, onLengthChanged, maxMeterDigits)
            } else if (selectableUnits[selectedUnitIndex.intValue] == LengthUnit.FOOT_AND_INCH) {
                LengthInputFootInch(currentLength, syncLength, onLengthChanged, maxFeetDigits)
            }


            Button(
                onClick = {
                    selectedUnitIndex.intValue =
                        (selectedUnitIndex.intValue + 1) % selectableUnits.size
                    onUnitChanged(selectableUnits[selectedUnitIndex.intValue])
                    onLengthChanged(null) // the view always start with an empty value
                },
                enabled = selectableUnits.size > 1,
                modifier = Modifier
                    .padding(5.dp)
                    .width(75.dp)
            ) {
                Text(selectableUnits[selectedUnitIndex.intValue].toString())
            }

            Spacer(Modifier.width(25.dp))

            if (showMeasureButton) {
                MeasureButton(onClick = takeMeasurementClick)
            }
        }

    }
}

// TODO Adjusted from InputValidators.kt acceptDecimalDigits(). Should this be put into a common file?
fun acceptDecimalDigits(string: String, beforeDecimalPoint: Int, afterDecimalPoint: Int): Boolean {
    if (!string.all { it.isDigit() || it == '.' || it == ',' }) return false;
    val texts = string.split(',', '.')
    if (texts.size > 2 || texts.isEmpty()) return false
    val before = texts[0]
    val after = if (texts.size > 1) texts[1] else ""
    return string.toDoubleOrNull() != null && after.length <= afterDecimalPoint && before.length <= beforeDecimalPoint
}

@Composable
private fun LengthInputMeters(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxMeterDigits: Pair<Int, Int>,
) {
    var lengthMeterText by remember { mutableStateOf("") }

    LaunchedEffect(currentLength, syncLength) {
        // Sync from the length only when explicitly allowed (e.g. when inserting data from AR measurement)
        if (syncLength && currentLength is LengthInMeters) {
            // Clamp meters to maximum allowed numbers
            val upper = 10.0.pow(maxMeterDigits.first) - 10.0.pow(-maxMeterDigits.second)
            val clamped = currentLength.meters.coerceIn(0.0..upper)
            lengthMeterText = String.format("%.2f", clamped)

            // For the unlikely case, that clamping has happened: Propagate change back to parent
            // so that when still published, the displayed value is used.
            if (clamped != currentLength.meters) {
                onLengthChanged(LengthInMeters(clamped))
            }
        }
    }

    TextField(
        value = lengthMeterText,
        onValueChange = { input: String ->
            if (input.isEmpty()) {
                onLengthChanged(null)
                lengthMeterText = input
            } else if (acceptDecimalDigits(
                    input,
                    maxMeterDigits.first,
                    maxMeterDigits.second
                )
            ) {
                onLengthChanged(LengthInMeters(input.toDouble()))
                lengthMeterText = input
            }
        },
        textStyle = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
        modifier = Modifier
            .width(130.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}

@Composable
private fun LengthInputFootInch(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxFeetDigits: Int,
    modifier: Modifier = Modifier,
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
            if (syncLength && currentLength is LengthInFeetAndInches) {
                // Clamp feet/inches to maximum allowed values
                val upper = 10.0.pow(maxFeetDigits).toInt() - 1
                val clampedFeet = currentLength.feet.coerceIn(0..upper)
                val clampedInch = currentLength.inches.coerceIn(0..11)
                feet = clampedFeet.toString()
                inches = clampedInch.toString()

                // For the unlikely case, that clamping has happened: Propagate change back to parent
                // so that when still published, the displayed value is used.
                if (clampedFeet != currentLength.feet || clampedInch != currentLength.inches) {
                    onLengthChanged(LengthInFeetAndInches(clampedFeet, clampedInch))
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
                val value = input.toIntOrNull();
                if (value != null && input.length <= maxFeetDigits) {
                    if (inches.isNotEmpty()) {
                        onLengthChanged(LengthInFeetAndInches(value, inches.toInt()))
                    }
                    feet = value.toString()
                }

            },
            textStyle = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .width(95.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("'", style = MaterialTheme.typography.h4, modifier = Modifier.padding(1.dp))

        TextField(
            value = inches,
            onValueChange = { input: String ->
                if (input.isEmpty()) {
                    onLengthChanged(null)
                    inches = ""
                }
                val value = input.toIntOrNull();
                if (value != null && value >= 0 && value < 12) {
                    if (feet.isNotEmpty()) {
                        onLengthChanged(LengthInFeetAndInches(feet.toInt(), value))
                    }
                    inches = value.toString()
                }
            },
            textStyle = MaterialTheme.typography.h4.copy(textAlign = TextAlign.Center),
            modifier = Modifier
                .width(75.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text("″", style = MaterialTheme.typography.h4, modifier = Modifier.padding(1.dp))

    }
}

@Composable
private fun MeasureButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }, modifier = Modifier.size(50.dp)) {
        MeasurementIcon()
    }
}

@Composable
@Preview(showBackground = true)
private fun LengthFormPreview() {
    val length = remember { mutableStateOf<Length?>(LengthInMeters(10.00)) }

    LengthForm(
        currentLength = length.value,
        syncLength = true,
        onLengthChanged = { length.value = it },
        maxFeetDigits = 3,
        maxMeterDigits = Pair(2, 2),
        selectableUnits = listOf(LengthUnit.METER),
        onUnitChanged = {},
        showMeasureButton = true,
        takeMeasurementClick = {},
        explanation = "ABC"
    )
}

@Composable
@Preview(showBackground = true)
private fun LengthInputMetersPreview() {
    val length = remember { mutableStateOf<Length?>(LengthInMeters(22.22)) }

    LengthInputMeters(
        currentLength = length.value,
        syncLength = true,
        onLengthChanged = {},
        maxMeterDigits = Pair(2, 2)
    )
}

@Composable
@Preview(showBackground = true)
private fun LengthInputFootInchPreview() {
    val length = remember { mutableStateOf<Length?>(LengthInFeetAndInches(999, 11)) }

    LengthInputFootInch(
        currentLength = length.value,
        syncLength = true,
        onLengthChanged = {},
        maxFeetDigits = 3
    )
}
