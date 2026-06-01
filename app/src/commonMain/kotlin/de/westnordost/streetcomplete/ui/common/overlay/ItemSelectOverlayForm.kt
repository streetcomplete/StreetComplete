package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites

/** Overlay form to select an item from a list of [selectableItems] (some [items] may not be
 *  selectable).
 *  It initially displays the [initialSelectedItem], clicking on it opens a dialog in which another
 *  item can be selected.
 *
 *  Additionally, previously picked items (persisted via [prefs] and [favoriteKey]) are displayed
 *  beneath that as chips. */
@Composable
inline fun <reified I> ItemSelectOverlayForm(
    itemsPerRow: Int,
    items: List<I>,
    initialSelectedItem: I?,
    noinline itemContent: @Composable (I) -> Unit,
    noinline lastPickedItemContent: @Composable (I) -> Unit,
    crossinline onClickOk: (selectedItem: I) -> Unit,
    prefs: Preferences,
    favoriteKey: String,
    modifier: Modifier = Modifier,
    selectableItems: List<I> = items,
    noinline otherAnswers: @Composable () -> List<AnswerItem> = { emptyList() },
) {
    val lastPicked = remember {
        prefs.getLastPicked<I>(favoriteKey).takeFavorites(n = 5, first = 1)
    }
    var selectedItem by rememberSerializable(initialSelectedItem) {
        mutableStateOf<I?>(initialSelectedItem)
    }

    var expanded by remember { mutableStateOf(false) }

    OverlayForm(
        isComplete = selectedItem != null && selectedItem in selectableItems,
        hasChanges = selectedItem != initialSelectedItem,
        onClickOk = {
            val value = selectedItem!!
            prefs.addLastPicked(favoriteKey, value)
            onClickOk(value)
        },
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = 96.dp),
                contentAlignment = Alignment.Center,
            ) {
                ItemCard(
                    item = selectedItem,
                    expanded = expanded,
                    onExpandChange = { expanded = it },
                    content = itemContent,
                )
            }
            if(lastPicked.isNotEmpty()) {
                LastPickedChipsRow(
                    items = lastPicked,
                    onClick = { selectedItem = it },
                    modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                    itemContent = lastPickedItemContent
                )
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }
    }
    if (expanded) {
        SimpleItemSelectDialog(
            onDismissRequest = { expanded = false },
            columns = SimpleGridCells.Fixed(itemsPerRow),
            items = items,
            onSelected = { selectedItem = it },
            itemContent = itemContent
        )
    }
}
