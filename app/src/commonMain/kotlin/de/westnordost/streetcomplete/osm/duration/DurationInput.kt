package de.westnordost.streetcomplete.osm.duration

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.unit_days
import de.westnordost.streetcomplete.resources.unit_hours
import de.westnordost.streetcomplete.resources.unit_minutes
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.input.DecimalInput
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Input field to input the elements of a duration (value + unit) */
@Composable
fun DurationInput(
    durationValue: Double?,
    onDurationValueChange: (Double?) -> Unit,
    durationUnit: DurationUnit,
    onSelectedDurationUnit: (DurationUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DecimalInput(
            value = durationValue,
            onValueChange = onDurationValueChange,
            maxIntegerDigits = 3,
            maxFractionDigits = 1,
            isUnsigned = true,
        )
        DropdownButton(
            items = DurationUnit.entries,
            onSelectedItem = onSelectedDurationUnit,
            selectedItem = durationUnit,
            itemContent = { Text(stringResource(it.text)) }
        )
    }
}

private val DurationUnit.text: StringResource get() = when (this) {
    DurationUnit.MINUTES -> Res.string.unit_minutes
    DurationUnit.HOURS -> Res.string.unit_hours
    DurationUnit.DAYS -> Res.string.unit_days
}
