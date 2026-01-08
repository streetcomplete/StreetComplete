package de.westnordost.streetcomplete.quests.parking_fee


import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.opening_hours.HierarchicOpeningHours
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

/** Input field to enter a time restriction (i.e. only at hours..., except at hours... or at any time) */
@Composable
fun TimeRestrictionInput(
    timeRestriction: TimeRestriction,
    onChangeTimeRestriction: (TimeRestriction) -> Unit,
    hours: HierarchicOpeningHours,
    onChangeHours: (HierarchicOpeningHours) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
    showAtAnyTime: Boolean = true,
) {
    val items = remember(showAtAnyTime) {
        if (showAtAnyTime) TimeRestriction.entries
        else listOf(TimeRestriction.ONLY_AT_HOURS, TimeRestriction.EXCEPT_AT_HOURS)
    }

    Column(modifier) {
        DropdownButton(
            items = items,
            onSelectedItem = onChangeTimeRestriction,
            selectedItem = timeRestriction,
            style = ButtonStyle.Outlined,
            itemContent = { Text(stringResource(it.text)) }
        )
        if (timeRestriction != TimeRestriction.AT_ANY_TIME) {
            OpeningHoursTable(
                openingHours = hours,
                onChange = onChangeHours,
                timeMode = TimeMode.Spans,
                countryInfo = countryInfo,
                addButtonContent = { Text(stringResource(Res.string.quest_fee_add_times)) },
                locale = locale,
                userLocale = userLocale,
            )
        }
    }
}

enum class TimeRestriction { AT_ANY_TIME, ONLY_AT_HOURS, EXCEPT_AT_HOURS }

private val TimeRestriction.text get() = when (this) {
    TimeRestriction.AT_ANY_TIME -> Res.string.at_any_time
    TimeRestriction.ONLY_AT_HOURS -> Res.string.only_at_hours
    TimeRestriction.EXCEPT_AT_HOURS -> Res.string.except_at_hours
}
