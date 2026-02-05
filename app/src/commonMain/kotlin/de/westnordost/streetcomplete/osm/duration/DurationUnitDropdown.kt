package de.westnordost.streetcomplete.osm.duration

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.unit_days
import de.westnordost.streetcomplete.resources.unit_hours
import de.westnordost.streetcomplete.resources.unit_minutes
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Dropdown with which to select a duration unit */
@Composable
fun DurationUnitDropdown(
    selectedDuration: DurationUnit,
    onSelectedDuration: (DurationUnit) -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownButton(
        items = DurationUnit.entries,
        onSelectedItem = { newUnit -> onSelectedDuration(newUnit) },
        modifier = modifier,
        selectedItem = selectedDuration,
        style = ButtonStyle.Outlined,
        itemContent = { Text(stringResource(it.text)) }
    )
}

private val DurationUnit.text: StringResource get() = when (this) {
    DurationUnit.MINUTES -> Res.string.unit_minutes
    DurationUnit.HOURS -> Res.string.unit_hours
    DurationUnit.DAYS -> Res.string.unit_days
}
