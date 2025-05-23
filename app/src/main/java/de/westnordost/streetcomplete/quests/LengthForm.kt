package de.westnordost.streetcomplete.quests

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.osm.LengthInFeetAndInches
import de.westnordost.streetcomplete.osm.LengthInMeters

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
) {
    val selectedUnitIndex = remember { mutableIntStateOf(0) }

    // Trigger unit changed event to sync with parent
    onUnitChanged(selectableUnits[selectedUnitIndex.intValue])

    Column() {
        if (explanation != null) {
            Text(
                text = explanation,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 0.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        )

        {
            if (selectableUnits[selectedUnitIndex.intValue] == LengthUnit.METER) {
                LengthInputMeters(currentLength, syncLength, onLengthChanged, maxMeterDigits)
            } else if (selectableUnits[selectedUnitIndex.intValue] == LengthUnit.FOOT_AND_INCH) {
                LengthInputFootInch(currentLength, onLengthChanged, maxFeetDigits)
            }

            Button(
                onClick = {
                    selectedUnitIndex.intValue =
                        (selectedUnitIndex.intValue + 1) % selectableUnits.size
                    onUnitChanged(selectableUnits[selectedUnitIndex.intValue])
                },
                enabled = selectableUnits.size > 1,
            ) {
                Text(selectableUnits[selectedUnitIndex.intValue].toString())
            }

            Spacer(modifier = Modifier.weight(1f))
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
fun LengthInputMeters(
    currentLength: Length?,
    syncLength: Boolean,
    onLengthChanged: (Length?) -> Unit,
    maxMeterDigits: Pair<Int, Int>,
    ) {
    var lengthMeterText by remember { mutableStateOf(currentLength?.toMeters()?.toString() ?: "") }

    LaunchedEffect(currentLength, syncLength) {
        // Sync from the length only when explicitly allowed (e.g. when inserting data from AR measurement)
        if (syncLength) {
            lengthMeterText = currentLength?.toMeters()?.toString() ?: ""
        }
    }

    Row() {
        TextField(
            value = lengthMeterText,
            onValueChange = { input: String ->
                if (input.isEmpty()) {
                    onLengthChanged(null)
                    lengthMeterText = input
                } else if (acceptDecimalDigits(input, maxMeterDigits.first, maxMeterDigits.second)) {
                    onLengthChanged(LengthInMeters(input.toDouble()))
                    lengthMeterText = input
                }
            },
            textStyle = TextStyle(
                fontSize = dimensionResource(id = R.dimen.x_large_input).value.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .width(150.dp)
                .padding(5.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

    }
}

@Composable
fun LengthInputFootInch(currentValue: Length?, onLengthChanged: (Length?) -> Unit, maxFeetDigits: Int) {
    Row {
        var feet by remember { mutableStateOf((currentValue as? LengthInFeetAndInches)?.feet?.toString() ?: "") }
        var inches by remember { mutableStateOf((currentValue as? LengthInFeetAndInches)?.inches?.toString() ?: "") }

        TextField(
            value = feet,
            onValueChange = { input: String ->
                if (input.isEmpty()) {
                    onLengthChanged(null)
                    feet = ""
                }
                val value = input.toIntOrNull();
                if(value != null && input.length <= maxFeetDigits) {
                    if (inches.isNotEmpty()) {
                        onLengthChanged(LengthInFeetAndInches(value, inches.toInt()))
                    }
                    feet = value.toString()

                }

            },
            textStyle = TextStyle(
                fontSize = dimensionResource(id = R.dimen.x_large_input).value.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .width(100.dp)
                .padding(5.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("", fontWeight = FontWeight.Bold, fontSize = 46.sp)

        TextField(
            value = inches,
            onValueChange = { input: String ->
                if (input.isEmpty()) {
                    onLengthChanged(null)
                    inches = ""
                }
                val value = input.toIntOrNull();
                if(value != null && value >= 0 && value < 12) {
                    if (feet.isNotEmpty()) {
                        onLengthChanged(LengthInFeetAndInches(feet.toInt(), value))
                    }
                    inches = value.toString()

                }
            },
            textStyle = TextStyle(
                fontSize = dimensionResource(id = R.dimen.x_large_input).value.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .width(100.dp)
                .padding(5.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Text("″", fontWeight = FontWeight.Bold, fontSize = 46.sp)

    }
}

@Composable
fun MeasureButton(onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Image(
            painter = painterResource(R.drawable.ic_camera_measure_24dp),
            contentDescription = null,
            modifier = Modifier.size(56.dp),
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun LengthFormPreview() {
    val length = remember { mutableStateOf<Length?>(LengthInMeters(2.0)) }

    LengthForm(
        currentLength = length.value,
        syncLength = false,
        onLengthChanged = { length.value = it },
        maxFeetDigits = 3,
        maxMeterDigits = Pair(2, 2),
        selectableUnits = listOf(LengthUnit.FOOT_AND_INCH),
        onUnitChanged = {},
        showMeasureButton = true,
        takeMeasurementClick = {},
        explanation = "ABC"
    )
}

@Composable
@Preview(showBackground = true)
private fun LengthInputMetersPreview() {
    val length = remember { mutableStateOf<Length?>(LengthInMeters(2.0)) }

    LengthInputMeters(currentLength = length.value, false, onLengthChanged = {}, maxMeterDigits = Pair(2,2))
}

@Composable
@Preview(showBackground = true)
private fun LengthInputFootInchPreview() {
    val length = remember { mutableStateOf<Length?>(LengthInFeetAndInches(2, 5)) }

    LengthInputFootInch(currentValue = length.value, onLengthChanged = {}, maxFeetDigits = 4)
}
