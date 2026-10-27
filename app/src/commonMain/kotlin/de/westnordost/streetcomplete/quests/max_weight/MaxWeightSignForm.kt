package de.westnordost.streetcomplete.quests.max_weight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults.BackgroundOpacity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.WeightMeasurementUnit
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.theme.largeInput
import org.jetbrains.compose.resources.painterResource

@Composable
fun MaxWeightRestrictionSignForm(
    value: MaxWeightRestriction,
    onValueChange: (MaxWeightRestriction) -> Unit,
    locale: Locale,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    when (value) {
        is MaxWeightCategorySweden -> {
            MaxWeightSignSwedenForm(
                value = value,
                onValueChange = onValueChange,
                modifier = modifier,
            )
        }
        else -> {
            MaxWeightSignForm(
                value = value,
                onWeightChange = {
                    onValueChange(when (value) {
                        is MaxAxleLoad -> value.copy(weight = it)
                        is MaxTandemAxleLoad -> value.copy(weight = it)
                        is MaxWeight -> value.copy(weight = it)
                        is MaxWeightRating -> value.copy(weight = it)
                        is MaxWeightRatingHgv -> value.copy(weight = it)
                    })
                },
                locale = locale,
                selectableUnits = selectableUnits,
                modifier = modifier,
            )
        }
    }
}

/** A form to input the [weight] for a single max weight sign [type]. The signs shown
 *  should look similar to how the signs actually look in the country with the given [locale]
 */
@Composable
private fun MaxWeightSignForm(
    value: MaxWeightRestriction,
    onWeightChange: (Weight?) -> Unit,
    locale: Locale,
    selectableUnits: List<WeightMeasurementUnit>,
    modifier: Modifier = Modifier,
) {
    val weight = value.weight
    var weightValue by remember { mutableStateOf(weight?.value) }
    var weightUnit by remember { mutableStateOf(
        weight?.unit ?: selectableUnits.firstOrNull() ?: WeightMeasurementUnit.METRIC_TON
    ) }
    if (weight != null) {
        if (weight.value != weightValue) weightValue = weight.value
        if (weight.unit != weightUnit) weightUnit = weight.unit
    }
    val onWeightValueChange = { newValue: Double? ->
        weightValue = newValue
        onWeightChange(newValue?.let { Weight(it, weightUnit) })
    }
    val onWeightUnitChange = { newUnit: WeightMeasurementUnit ->
        weightUnit = newUnit
        onWeightChange(weightValue?.let { Weight(it, newUnit) })
    }

    val countryCode = locale.region

    val color =
        if (countryCode in listOf("FI", "IS", "SE")) TrafficSignColor.Yellow
        else TrafficSignColor.White

    val unitTextStyle = MaterialTheme.typography.largeInput

    val weightInput: @Composable () -> Unit = {
        WeightInput(
            value = weightValue,
            onValueChange = onWeightValueChange,
            locale = locale
        )
    }

    val weightInputMutcd: @Composable () -> Unit = {
        WeightInputMutcd(
            weightValue, weightUnit, onWeightValueChange, onWeightUnitChange, selectableUnits, unitTextStyle,
            locale = locale
        )
    }

    Box(modifier) {
        when (value) {
            is MaxWeight -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("WEIGHT LIMIT") { weightInputMutcd() }
                else ->
                    MaxWeightSign(color = color) { weightInput() }
            }
            is MaxWeightRating -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("GVWR LIMIT") { weightInputMutcd() }
                "DE" ->
                    MaxWeightSignExtra(
                        color = color,
                        signContent = {},
                        extraContent = { weightInput() },
                    )
                "GB" ->
                    MaxWeightSign(color = color) {
                        weightInput()
                        Text("m g w")
                    }
                // French max weight sign is actually a max weight rating sign
                "FR" ->
                    MaxWeightSign(color = color) { weightInput() }
            }
            is MaxWeightRatingHgv -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("TRUCK GVWR LIMIT") { weightInputMutcd() }
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
                        extraContent = { weightInput() },
                    )
                else ->
                    MaxWeightSign(color = color) {
                        Icon(painterResource(Res.drawable.maxweight_hgv), null)
                        weightInput()
                    }
            }
            is MaxAxleLoad -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("AXLE WEIGHT LIMIT") { weightInputMutcd() }
                else ->
                    MaxWeightSign(color = color) {
                        weightInput()
                        Icon(painterResource(Res.drawable.maxweight_axleload), null)
                    }
            }
            is MaxTandemAxleLoad -> when (countryCode) {
                "AU", "CA", "US" ->
                    MaxWeightSignMutcd("TANDEM AXLE WEIGHT LIMIT") { weightInputMutcd() }
                else ->
                    MaxWeightSign(color = color) {
                        weightInput()
                        Icon(painterResource(Res.drawable.maxweight_bogieweight), null)
                    }
            }
            else -> throw IllegalStateException()
        }
    }
}

@Composable
private fun MaxWeightSignSwedenForm(
    value: MaxWeightCategorySweden,
    onValueChange: (MaxWeightCategorySweden) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        MaxWeightSign(color = TrafficSignColor.Yellow) {
            Icon(painterResource(Res.drawable.maxweight_hgv_se), null)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text("BK")
                DropdownButton(
                    items = MaxWeightCategorySweden.Category.entries,
                    selectedItem = value.category,
                    onSelectedItem = { onValueChange(MaxWeightCategorySweden(it)) },
                    itemContent = { Text(it.value.toString()) },
                    style = ButtonStyle.Text,
                    modifier = Modifier.background(
                        color = MaterialTheme.colors.onSurface.copy(alpha = BackgroundOpacity),
                        shape = MaterialTheme.shapes.small
                    )
                ) {
                    ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )) {
                        Text(value.category?.value?.toString() ?: " ")
                    }
                }
            }
        }
    }
}


@Preview
@Composable
private fun MaxWeightSignSignFormPreview() {
    var country by remember { mutableStateOf("DE") }
    val countries = remember { listOf("DE", "FI", "US", "SE") }

    var maxWeight by remember { mutableStateOf<MaxWeightRestriction>(MaxWeight()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.safeContentPadding()
    ) {
        DropdownButton(
            items = countries,
            onSelectedItem = { country = it },
            selectedItem = country,
            itemContent = { Text(it) }
        )
        DropdownButton(
            items = MaxWeightRestriction.getSelectableEntries(country),
            onSelectedItem = { maxWeight = it },
            selectedItem = maxWeight,
            itemContent = { Text(it::class.simpleName!!) }
        )
        MaxWeightRestrictionSignForm(
            value = maxWeight,
            onValueChange = { maxWeight = it },
            locale = Locale("${Locale.current.language}-$country"),
            selectableUnits = WeightMeasurementUnit.entries,
        )
    }
}
