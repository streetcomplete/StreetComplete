package de.westnordost.streetcomplete.ui.common.item_select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import de.westnordost.streetcomplete.ui.ktx.selectionFrame

/** Vertical grid of items that can each be selected */
@Composable
fun <I> ItemsSelect(
    columns: SimpleGridCells,
    items: List<I>,
    selectedItems: Set<I>,
    onSelect: (item: I, selected: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: I) -> Unit,
) {
    VerticalGrid(
        columns = columns,
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (item in items) {
            val isSelected = item in selectedItems
            Box(
                modifier = Modifier
                    .selectionFrame(isSelected)
                    .toggleable(isSelected) { onSelect(item, it) },
                contentAlignment = Alignment.Center,
            ) {
                itemContent(item)
            }
        }
    }
}
