package de.westnordost.streetcomplete.ui.common.item_select

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import de.westnordost.streetcomplete.ui.ktx.selectionFrame

/** Vertical grid of items where one item can be selected */
@Composable
fun <I> ItemSelect(
    columns: SimpleGridCells,
    items: List<I>,
    selectedItem: I?,
    onSelect: (item: I?) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: I) -> Unit,
) {
    VerticalGrid(
        columns = columns,
        modifier = modifier
    ) {
        for (item in items) {
            val isSelected = item == selectedItem
            Box(Modifier
                .selectionFrame(isSelected)
                .selectable(isSelected) { onSelect(if (isSelected) null else item) }
            ) {
                itemContent(item)
            }
        }
    }
}
