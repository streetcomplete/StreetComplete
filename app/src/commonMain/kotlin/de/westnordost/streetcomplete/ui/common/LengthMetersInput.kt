package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.Length
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.util.ktx.toShortString
import androidx.compose.ui.tooling.preview.Preview

/** Input field to input a length in meters */
@Composable
fun LengthMetersInput(
    length: Length.Meters?,
    onChange: (Length.Meters?) -> Unit,
    maxMeterDigits: Pair<Int, Int>,
    modifier: Modifier = Modifier,
    style: TextFieldStyle = TextFieldStyle.Filled,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AutoFitTextFieldFontSize(
            value = length?.meters?.toShortString().orEmpty(),
            modifier = Modifier.weight(1f)
        ) {
            DecimalInput(
                value = length?.meters,
                onValueChange = { meters ->
                    onChange(meters?.let { Length.Meters(it) })
                },
                modifier = Modifier.fillMaxWidth(),
                maxIntegerDigits = maxMeterDigits.first,
                maxFractionDigits = maxMeterDigits.second,
                isUnsigned = true,
                style = style,
            )
        }
        Text("m")
    }
}

@Composable @Preview
private fun LengthMetersInputPreview() {
    var meters: Length.Meters? by remember { mutableStateOf(Length.Meters(3.0)) }
    LengthMetersInput(
        length = meters,
        onChange = { meters = it },
        maxMeterDigits = Pair(2, 2),
    )
}
