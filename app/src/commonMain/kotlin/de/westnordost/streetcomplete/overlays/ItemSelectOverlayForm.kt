package de.westnordost.streetcomplete.overlays

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
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow

/** Overlay form to select an item from a list of [items].
 *  It displays the currently [selectedItem], clicking on it opens a dialog in which another item
 *  can be selected.
 *  Additionally, previously picked items (or suggestions) are displayed beneath that, as a sort of
 *  shortcut (to save one click). */
@Composable
fun <I> ItemSelectOverlayForm(
    itemsPerRow: Int,
    items: List<I>,
    itemContent: @Composable (I) -> Unit,
    selectedItem: I?,
    lastPickedItems: List<I>,
    lastPickedItemContent: @Composable (I) -> Unit,
    onSelectItem: (I) -> Unit,
    modifier: Modifier = Modifier,
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
            var expanded by remember { mutableStateOf(false) }

            ItemCard(
                item = selectedItem,
                expanded = expanded,
                onExpandChange = { expanded = it },
                content = itemContent,
            )
            if (expanded) {
                SimpleItemSelectDialog(
                    onDismissRequest = { expanded = false },
                    columns = SimpleGridCells.Fixed(itemsPerRow),
                    items = items,
                    onSelected = onSelectItem,
                    itemContent = itemContent
                )
            }
        }
        if(lastPickedItems.isNotEmpty()) {
            LastPickedChipsRow(
                items = lastPickedItems,
                onClick = onSelectItem,
                modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                itemContent = lastPickedItemContent
            )
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}
