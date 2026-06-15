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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.duration.DurationUnit
import de.westnordost.streetcomplete.osm.duration.DurationUnitDropdown
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import de.westnordost.streetcomplete.util.locale.CurrencyFormatElements

/**
 * A UI component that allows users to input a monetary charge amount associated with a duration unit
 */
@Composable
fun ChargeInput(
    amount: Double,
    onAmountChange: (Double?) -> Unit,
    currencyFormatInfo: CurrencyFormatElements,
    durationUnit: DurationUnit,
    onDurationUnitChange: (DurationUnit) -> Unit,
    perLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currencyFormatInfo.isSymbolBeforeAmount) {
            Text(
                text = currencyFormatInfo.symbol,
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        DecimalInput(
            value = amount,
            onValueChange = onAmountChange,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (currencyFormatInfo.decimalDigits > 0) {
                    KeyboardType.Decimal
                } else {
                    KeyboardType.Number
                }
            ),
            modifier = Modifier.width(150.dp),
        )

        if (!currencyFormatInfo.isSymbolBeforeAmount) {
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
            DurationUnitDropdown(
                selectedDuration = durationUnit,
                onSelectedDuration = onDurationUnitChange,
                alwaysSingular = true
            )
        }
    }
}
