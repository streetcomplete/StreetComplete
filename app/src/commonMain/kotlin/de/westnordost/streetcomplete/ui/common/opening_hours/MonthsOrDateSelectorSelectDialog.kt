package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.streetcomplete.osm.opening_hours.getMonths
import de.westnordost.streetcomplete.osm.opening_hours.toMonthsSelectors
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.quest_openingHours_chooseMonthsTitle
import de.westnordost.streetcomplete.resources.quest_openingHours_chooseWeekdaysTitle
import de.westnordost.streetcomplete.resources.quest_opening_hours_two_languages
import de.westnordost.streetcomplete.ui.common.CheckboxGroup
import de.westnordost.streetcomplete.ui.common.dialogs.ScrollableAlertDialog
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a number of months */
@Composable
fun MonthsOrDateSelectorSelectDialog(
    onDismissRequest: () -> Unit,
    initialMonths: List<MonthsOrDateSelector>,
    onSelected: (months: List<MonthsOrDateSelector>) -> Unit,
    modifier: Modifier = Modifier,
    locale: Locale = Locale.current,
    userLocale: Locale = Locale.current,
) {
    val scrollState = rememberScrollState()
    val selection = remember(initialMonths) {
        SnapshotStateSet<Month>().also { it.addAll(initialMonths.getMonths()) }
    }

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(Res.string.quest_openingHours_chooseMonthsTitle)) },
        content = {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                LocalTextStyle provides MaterialTheme.typography.body1
            ) {
                CheckboxGroup(
                    options = Month.entries,
                    onSelectionChange = { month, checked ->
                        if (checked) selection.add(month)
                        else selection.remove(month)
                    },
                    selectedOptions = selection,
                    itemContent = {
                        val text = it.getDisplayName(locale = locale)
                        val textInUserLocale = it.getDisplayName(locale = userLocale)
                        if (text != textInUserLocale) {
                            Text(stringResource(Res.string.quest_opening_hours_two_languages, text, textInUserLocale))
                        } else {
                            Text(text)
                        }
                    },
                    modifier = Modifier
                        .fadingVerticalScrollEdges(scrollState, 32.dp)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(scrollState)
                )
            }
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(
                enabled = selection.isNotEmpty(),
                onClick = {
                    onSelected(selection.toMonthsSelectors())
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.ok))
            }
        }
    )
}
