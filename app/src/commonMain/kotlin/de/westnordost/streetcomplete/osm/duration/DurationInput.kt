package de.westnordost.streetcomplete.osm.duration

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.unit_days
import de.westnordost.streetcomplete.resources.unit_hours
import de.westnordost.streetcomplete.resources.unit_minutes
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
            modifier = Modifier.weight(1f),
        )
        DropdownButton(
            items = DurationUnit.entries,
            onSelectedItem = { newUnit ->
                unit = newUnit
                onChange(value?.let { Duration(it, newUnit) })
            },
            selectedItem = unit,
            style = ButtonStyle.Outlined,
            itemContent = { Text(stringResource(it.text)) }
        )
    }
}

private val DurationUnit.text: StringResource get() = when (this) {
    DurationUnit.MINUTES -> Res.string.unit_minutes
    DurationUnit.HOURS -> Res.string.unit_hours
    DurationUnit.DAYS -> Res.string.unit_days
}
