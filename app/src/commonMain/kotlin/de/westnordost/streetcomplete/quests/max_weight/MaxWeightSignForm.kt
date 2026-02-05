package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.quests.max_weight.MaxWeightType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.maxweight_axleload
import de.westnordost.streetcomplete.resources.maxweight_bogieweight
import de.westnordost.streetcomplete.resources.maxweight_hgv
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

/** A form to input the [weight] for a single max weight sign [type]. The signs shown
 *  should look similar to how the signs actually look in the country with the given [countryCode]
 */
@Composable
fun MaxWeightSignForm(
    type: MaxWeightType,
    weight: Weight?,
    onWeightChange: (Weight?) -> Unit,
    countryCode: String,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    var value by remember { mutableStateOf(weight?.value) }
    var unit by remember { mutableStateOf(
        weight?.unit ?: selectableUnits.firstOrNull() ?: WeightMeasurementUnit.METRIC_TON
    ) }
    if (weight != null) {
        if (weight.value != value) value = weight.value
        if (weight.unit != unit) unit = weight.unit
    }
    val onValueChange = { newValue: Double? ->
        value = newValue
        onWeightChange(newValue?.let { Weight(it, unit) })
    }
    val onUnitChange = { newUnit: WeightMeasurementUnit ->
        unit = newUnit
        onWeightChange(value?.let { Weight(it, newUnit) })
    }

    val color =
        if (countryCode in listOf("FI", "IS", "SE")) TrafficSignColor.Yellow
        else TrafficSignColor.White

    val unitTextStyle = MaterialTheme.typography.largeInput

    Box(modifier) {
        when (type) {
            MAX_WEIGHT -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("WEIGHT LIMIT") {
                        WeightInputMutcd(value, unit, onValueChange, onUnitChange, selectableUnits, unitTextStyle)
                    }
                else ->
                    MaxWeightSign(color = color) {
                        WeightInput(value, onValueChange)
                    }
            }
            MAX_WEIGHT_RATING -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("GVWR LIMIT") {
                        WeightInputMutcd(value, unit, onValueChange, onUnitChange, selectableUnits, unitTextStyle)
                    }
                "DE" ->
                    MaxWeightSignExtra(
                        color = color,
                        signContent = {},
                        extraContent = { WeightInput(value, onValueChange) },
                    )
                "GB" ->
                    MaxWeightSign(color = color) {
                        WeightInput(value, onValueChange)
                        Text("m g w")
                    }
                // French max weight sign is actually a max weight rating sign
                "FR" ->
                    MaxWeightSign(color = color) {
                        WeightInput(value, onValueChange)
                    }
            }
            MAX_WEIGHT_RATING_HGV -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("TRUCK GVWR LIMIT") {
                        WeightInputMutcd(value, unit, onValueChange, onUnitChange, selectableUnits, unitTextStyle)
                    }
                "DE" ->
                    MaxWeightSignExtra(
                        color = color,
                        signContent = {
                            Icon(
                                painter = painterResource(Res.drawable.maxweight_hgv),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(16.dp)
                            )
                        },
                        extraContent = { WeightInput(value, onValueChange) },
                    )
                else ->
                    MaxWeightSign(color = color) {
                        Icon(painterResource(Res.drawable.maxweight_hgv), null)
                        WeightInput(value, onValueChange)
                    }
            }
            MAX_AXLE_LOAD -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("AXLE WEIGHT LIMIT") {
                        WeightInputMutcd(value, unit, onValueChange, onUnitChange, selectableUnits, unitTextStyle)
                    }
                else ->
                    MaxWeightSign(color = color) {
                        WeightInput(value, onValueChange)
                        Icon(painterResource(Res.drawable.maxweight_axleload), null)
                    }
            }
            MAX_TANDEM_AXLE_LOAD -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("TANDEM AXLE WEIGHT LIMIT") {
                        WeightInputMutcd(value, unit, onValueChange, onUnitChange, selectableUnits, unitTextStyle)
                    }
                else ->
                    MaxWeightSign(color = color) {
                        WeightInput(value, onValueChange)
                        Icon(painterResource(Res.drawable.maxweight_bogieweight), null)
                    }
            }
        }
    }
}

@Preview
@Composable
private fun MaxWeightSignFormPreview() {
    var country by remember { mutableStateOf("DE") }
    val countries = remember { listOf("DE", "FI", "US") }

    var maxWeightType by remember { mutableStateOf(MAX_WEIGHT) }

    var weight by remember { mutableStateOf<Weight?>(null) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        DropdownButton(
            items = countries,
            onSelectedItem = { country = it },
            selectedItem = country,
            itemContent = { Text(it) }
        )
        DropdownButton(
            items = MaxWeightType.entries,
            onSelectedItem = { maxWeightType = it },
            selectedItem = maxWeightType,
            itemContent = { Text(it.name) }
        )
        MaxWeightSignForm(
            type = maxWeightType,
            weight = weight,
            onWeightChange = { weight = it },
            countryCode = country,
            selectableUnits = WeightMeasurementUnit.entries,
        )
    }
}
