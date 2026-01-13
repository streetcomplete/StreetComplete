package de.westnordost.streetcomplete.osm.time_restriction


import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
import de.westnordost.streetcomplete.osm.time_restriction.TimeRestriction.Mode.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.at_any_time
import de.westnordost.streetcomplete.resources.except_at_hours
import de.westnordost.streetcomplete.resources.only_at_hours
import de.westnordost.streetcomplete.resources.quest_fee_add_times
import de.westnordost.streetcomplete.ui.common.ButtonStyle
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.opening_hours.OpeningHoursTable
import de.westnordost.streetcomplete.ui.common.opening_hours.TimeMode
import org.jetbrains.compose.resources.stringResource

/** Input field to enter a time restriction (i.e. only at hours..., except at hours... or null for
 *  no time restriction.) */
@Composable
fun TimeRestrictionInput(
    timeRestriction: TimeRestriction?,
    onChange: (TimeRestriction?) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
    allowSelectNoRestriction: Boolean = true,
) {
    val modes = remember(allowSelectNoRestriction) {
        buildList {
            if (allowSelectNoRestriction) add(null)
            addAll(TimeRestriction.Mode.entries)
        }
    }

    Column(modifier) {
        DropdownButton(
            items = modes,
            onSelectedItem = { newMode ->
                onChange(newMode?.let {
                    TimeRestriction(timeRestriction?.hours ?: HierarchicOpeningHours(), it)
                })
            },
            selectedItem = timeRestriction?.mode,
            style = ButtonStyle.Outlined,
            itemContent = { Text(stringResource(it.text)) },
            content = { Text(stringResource(timeRestriction?.mode.text)) }
        )

        if (timeRestriction != null) {
            OpeningHoursTable(
                openingHours = timeRestriction.hours,
                onChange = { onChange(timeRestriction.copy(hours = it)) },
                timeMode = TimeMode.Spans,
                countryInfo = countryInfo,
                addButtonContent = { Text(stringResource(Res.string.quest_fee_add_times)) },
                locale = countryInfo.userPreferredLocale,
                userLocale = Locale.current,
            )
        }
    }
}

private val TimeRestriction.Mode?.text get() = when (this) {
    null -> Res.string.at_any_time
    ONLY_AT_HOURS -> Res.string.only_at_hours
    EXCEPT_AT_HOURS -> Res.string.except_at_hours
}
