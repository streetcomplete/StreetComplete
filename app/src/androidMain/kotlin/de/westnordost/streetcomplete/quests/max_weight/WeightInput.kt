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
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.ui.common.AutoFitTextFieldFontSize
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.SelectButton
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.util.ktx.toShortString

/** Weight input for MUTCD (-inspired) countries. It is possible that different units are used,
 *  e.g. pounds / (short) tons in the US, metric tons anywhere else. */
@Composable
fun WeightInputMutcd(
    value: Double?,
    unit: WeightMeasurementUnit,
    onValueChange: (Double?) -> Unit,
    onUnitChange: (WeightMeasurementUnit) -> Unit,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AutoFitTextFieldFontSize(value?.toShortString().orEmpty()) {
            DecimalInput(
                value = value,
                onValueChange = onValueChange,
                maxIntegerDigits = 6,
                maxFractionDigits = 1,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (selectableUnits.size > 1) {
            SelectButton(
                items = selectableUnits,
                onSelectedItem = onUnitChange,
                selectedItem = unit,
                style = ButtonStyle.Text,
                itemContent = { Text(it.displayString.uppercase()) }
            )
        } else {
            Text(unit.displayString.uppercase())
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
            value = value?.toShortString().orEmpty(),
            modifier = Modifier.width(96.dp)
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
