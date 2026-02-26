package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit
import de.westnordost.streetcomplete.ui.common.AutoFitTextFieldFontSize
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.TextField2
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Speed input. A unit switcher is shown if there are several different speed units */
@Composable
fun SpeedInput(
    value: Int?,
    unit: SpeedMeasurementUnit,
    onValueChange: (Int?) -> Unit,
    onUnitChange: (SpeedMeasurementUnit) -> Unit,
    selectableUnits: List<SpeedMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val stringValue = value?.toString().orEmpty()
        AutoFitTextFieldFontSize(
            value = stringValue,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField2(
                value = stringValue,
                onValueChange = { newText: String ->
                    if (newText.isEmpty()) {
                        onValueChange(null)
                    } else {
                        newText.toIntOrNull()?.let { onValueChange(it) }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (selectableUnits.size > 1) {
            DropdownButton(
                items = selectableUnits,
                onSelectedItem = onUnitChange,
                selectedItem = unit,
                style = ButtonStyle.Text,
                itemContent = {
                    Text(
                        text = it.displayString,
                        style = MaterialTheme.typography.largeInput,
                    )
                }
            )
        }
    }
}
