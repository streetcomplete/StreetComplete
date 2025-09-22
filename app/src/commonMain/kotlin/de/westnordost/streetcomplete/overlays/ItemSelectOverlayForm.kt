package de.westnordost.streetcomplete.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectButton
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.ktx.pxToDp

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
    var hasPicked by rememberSaveable { mutableStateOf(false) }
    var lastPickedRowHeight by rememberSaveable { mutableIntStateOf(0) }

    fun onSelected(item: I) {
        hasPicked = true
        onSelectItem(item)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.defaultMinSize(minHeight = 128.dp),
            contentAlignment = Alignment.Center,
        ) {
            ItemSelectButton(
                columns = SimpleGridCells.Fixed(itemsPerRow),
                items = items,
                onSelected = ::onSelected,
                selectedItem = selectedItem,
                itemContent = itemContent,
            )
        }
        if(lastPickedItems.isNotEmpty() && !hasPicked) {
            LastPickedChipsRow(
                items = lastPickedItems,
                onClick = ::onSelected,
                modifier = Modifier
                    .padding(horizontal = 56.dp)
                    .onSizeChanged { lastPickedRowHeight = it.height }
                ,
                itemContent = lastPickedItemContent
            )
        } else {
            Spacer(Modifier.height(lastPickedRowHeight.pxToDp()))
        }
    }
}
