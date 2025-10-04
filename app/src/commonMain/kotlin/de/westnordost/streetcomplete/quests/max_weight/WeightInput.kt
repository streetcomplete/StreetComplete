package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.ui.common.AutoFitTextFieldFontSize
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.input.DecimalInput

/** Weight input for MUTCD (-inspired) countries. It is possible that different units are used,
 *  e.g. pounds / (short) tons in the US, metric tons anywhere else. */
@Composable
fun WeightInputMutcd(
    value: Double?,
    unit: WeightMeasurementUnit,
    onValueChange: (Double?) -> Unit,
    onUnitChange: (WeightMeasurementUnit) -> Unit,
    selectableUnits: List<WeightMeasurementUnit>,
    unitTextStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AutoFitTextFieldFontSize(value?.toString().orEmpty()) {
            DecimalInput(
                value = value,
                onValueChange = onValueChange,
                maxIntegerDigits = 6,
                maxFractionDigits = 1,
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
                        text = it.displayString.uppercase(),
                        style = unitTextStyle,
                    )
                }
            )
        } else {
            Text(
                text = unit.displayString.uppercase(),
                style = unitTextStyle,
            )
        }
    }
}

/** Normal weight input Weight in tons always */
@Composable
fun WeightInput(
    value: Double?,
    onValueChange: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(" ") // just so that the input is centered
        AutoFitTextFieldFontSize(
            value = value?.toString().orEmpty(),
            modifier = Modifier.width(112.dp)
        ) {
            DecimalInput(
                value = value,
                onValueChange = onValueChange,
                maxIntegerDigits = 3,
                maxFractionDigits = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text("t")
    }
}
