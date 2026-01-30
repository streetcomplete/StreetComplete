package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.duration.DurationUnit
import de.westnordost.streetcomplete.util.locale.CurrencyFormatElements
// import org.jetbrains.compose.ui.tooling.preview.Preview

/** A composable for inputting a charge amount with currency symbol and time unit selector */
@Composable
fun ChargeInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    currencyFormatInfo: CurrencyFormatElements,
    durationUnit: DurationUnit,
    onDurationUnitChange: (DurationUnit) -> Unit,
    perLabel: String,
    modifier: Modifier = Modifier,
    durationUnitDisplayNames: (DurationUnit) -> String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currencyFormatInfo.symbolBeforeAmount) {
            Text(
                text = currencyFormatInfo.symbol,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        TextField(
            value = amount,
            onValueChange = onAmountChange,
            placeholder = {
                // Generate placeholder based on decimal places
                val placeholderValue = when (currencyFormatInfo.decimalPlaces) {
                    0 -> "150"
                    1 -> "15.0"
                    else -> "1.50"
                }
                Text(placeholderValue)
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (currencyFormatInfo.decimalPlaces > 0) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Number
                }
            ),
            modifier = Modifier.width(150.dp),
            singleLine = true,
        )

        if (!currencyFormatInfo.symbolBeforeAmount) {
            Text(
                text = currencyFormatInfo.symbol,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Text(
            text = perLabel,
            style = MaterialTheme.typography.body1
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            /*
            DurationUnitDropdown(
                selectedDuration = durationUnit,
                onSelectedDuration = onDurationUnitChange,
            )*/
            DropdownButton(
                items = DurationUnit.entries,
                onSelectedItem = onDurationUnitChange,
                itemContent = { unit ->
                    Text(durationUnitDisplayNames(unit))
                },
                selectedItem = durationUnit,
                style = ButtonStyle.Outlined,
            )
        }
    }
}
/*
@Composable
@Preview
private fun ChargeInputPreview() {
    val amount = remember { mutableStateOf("1.50") }
    val durationUnit = remember { mutableStateOf(DurationUnit.HOURS) }

    ChargeInput(
        amount = amount.value,
        onAmountChange = { amount.value = it },
        currencyFormatInfo = CurrencyFormatElements(
            symbol = "€",
            symbolBeforeAmount = false,
            decimalPlaces = 2
        ),
        durationUnit = durationUnit.value,
        onDurationUnitChange = { durationUnit.value = it },
        perLabel = "per",
        durationUnitDisplayNames = { unit ->
            when (unit) {
                DurationUnit.MINUTES -> Res.string.unit_minutes
                DurationUnit.HOURS -> Res.string.unit_hours
                DurationUnit.DAYS -> Res.string.unit_days
            } as String
        }
    )
}
*/
