package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateSet
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.westnordost.osm_opening_hours.model.Month
import de.westnordost.osm_opening_hours.model.MonthRange
import de.westnordost.osm_opening_hours.model.MonthsOrDateSelector
import de.westnordost.osm_opening_hours.model.SingleMonth
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.quest_openingHours_chooseWeekdaysTitle
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialogLayout
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import org.jetbrains.compose.resources.stringResource

/** Dialog in which to select a number of months */
@Composable
fun MonthsSelectDialog(
    onDismissRequest: () -> Unit,
    selectedMonths: List<MonthsOrDateSelector>,
    onSelected: (months: List<MonthsOrDateSelector>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.current
    val scrollState = rememberScrollState()
    val monthsSelection = remember(selectedMonths) {
        SnapshotStateSet<Month>().also { it.addAll(selectedMonths.getSelectedMonths()) }
    }

    fun toggleMonth(checked: Boolean, month: Month) {
        if (checked) monthsSelection.add(month)
        else monthsSelection.remove(month)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        AlertDialogLayout(
            modifier = modifier,
            title = { Text(stringResource(Res.string.quest_openingHours_chooseWeekdaysTitle)) },
            content = {
                Column(Modifier
                    .fadingVerticalScrollEdges(scrollState, 64.dp)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                ) {
                    for (month in Month.entries) {
                        val checked = month in monthsSelection
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .toggleable(checked) { toggleMonth(it, month) }
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { toggleMonth(it, month) },
                            )
                            Text(month.getDisplayName(locale = locale))
                        }
                    }
                }
            },
            buttons = {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(Res.string.cancel))
                }
                TextButton(onClick = {
                    onDismissRequest()
                    onSelected(monthsSelection.toMonthsSelectors())
                }) {
                    Text(stringResource(Res.string.ok))
                }
            }
        )
    }
}

private fun Set<Month>.toMonthsSelectors(): List<MonthsOrDateSelector> =
    toOrdinalRanges(Month.entries).flatMap {
        val start = it.start
        val end = it.endInclusive
        if (start == end) {
            listOf(SingleMonth(Month.entries[start]))
        } else if (start + 1 == end) {
            listOf(SingleMonth(Month.entries[start]), SingleMonth(Month.entries[end]))
        } else {
            listOf(MonthRange(Month.entries[start], Month.entries[end]))
        }
    }
