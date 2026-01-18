package de.westnordost.streetcomplete.osm.duration

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Input field to input the elements of a duration (value + unit) */
@Composable
fun DurationInput(
    duration: Duration?,
    onChange: (Duration?) -> Unit,
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(duration?.value) }
    var unit by remember { mutableStateOf(duration?.unit ?: DurationUnit.MINUTES) }
    if (duration != null) {
        if (duration.value != value) value = duration.value
        if (duration.unit != unit) unit = duration.unit
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DecimalInput(
            value = value,
            onValueChange = { newValue ->
                value = newValue
                onChange(newValue?.let { Duration(it, unit) })
            },
            maxIntegerDigits = 3,
            maxFractionDigits = 1,
            isUnsigned = true,
            textStyle = MaterialTheme.typography.largeInput,
            modifier = Modifier.width(96.dp),
        )
        DurationUnitDropdown(
            selectedDuration = unit,
            onSelectedDuration = { newUnit ->
                unit = newUnit
                onChange(value?.let { Duration(it, newUnit) })
            }
        )
    }
}
