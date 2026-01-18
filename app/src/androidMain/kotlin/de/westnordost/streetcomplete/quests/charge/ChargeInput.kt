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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/** Composable for inputting a charge amount with currency symbol and a time unit selector */
@Composable
fun ChargeInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    currencySymbol: String,
    timeUnit: TimeUnit,
    onTimeUnitChange: (TimeUnit) -> Unit,
    perLabel: String,
    modifier: Modifier = Modifier,
    timeUnitDisplayNames: (TimeUnit) -> String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = amount,
            onValueChange = onAmountChange,
            placeholder = { Text("1.50") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(150.dp),
            singleLine = true,
        )

        Text(
            text = currencySymbol,
            style = MaterialTheme.typography.h5,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 8.dp)
        )

        Text(
            text = perLabel,
            style = MaterialTheme.typography.body1
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DropdownButton(
                items = TimeUnit.entries,
                selectedItem = timeUnit,
                onSelectedItem = onTimeUnitChange,
                itemContent = { unit ->
                    Text(timeUnitDisplayNames(unit))
                },
            )
        }
    }
}

enum class TimeUnit {
    HOUR,
    DAY,
    MINUTES_30,
    MINUTES_15;

    fun toOsmValue(): String = when (this) {
        HOUR -> "hour"
        DAY -> "day"
        MINUTES_30 -> "30 minutes"
        MINUTES_15 -> "15 minutes"
    }
}

@Composable
@Preview
private fun ChargeInputPreview() {
    val amount = remember { mutableStateOf("1.50") }
    val timeUnit = remember { mutableStateOf(TimeUnit.HOUR) }

    ChargeInput(
        amount = amount.value,
        onAmountChange = { amount.value = it },
        currencySymbol = "€",
        timeUnit = timeUnit.value,
        onTimeUnitChange = { timeUnit.value = it },
        perLabel = "per",
        timeUnitDisplayNames = { unit ->
            when (unit) {
                TimeUnit.HOUR -> "hour"
                TimeUnit.DAY -> "day"
                TimeUnit.MINUTES_30 -> "30 min"
                TimeUnit.MINUTES_15 -> "15 min"
            }
        }
    )
}
