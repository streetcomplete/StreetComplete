package de.westnordost.streetcomplete.osm.duration

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Dropdown with which to select a duration unit */
@Composable
fun DurationUnitDropdown(
    selectedDuration: DurationUnit,
    onSelectedDuration: (DurationUnit) -> Unit,
    alwaysSingular: Boolean = false,
    modifier: Modifier = Modifier
) {
    DropdownButton(
        items = DurationUnit.entries,
        onSelectedItem = { newUnit -> onSelectedDuration(newUnit) },
        modifier = modifier,
        selectedItem = selectedDuration,
        style = ButtonStyle.Outlined,
        itemContent = { Text(stringResource(it.getText(alwaysSingular))) }
    )
}
private fun DurationUnit.getText(useSingular: Boolean): StringResource = when (this) {
    DurationUnit.MINUTES -> if (useSingular) Res.string.unit_minute else Res.string.unit_minutes
    DurationUnit.HOURS -> if (useSingular) Res.string.unit_hour else Res.string.unit_hours
    DurationUnit.DAYS -> if (useSingular) Res.string.unit_day else Res.string.unit_days
}
