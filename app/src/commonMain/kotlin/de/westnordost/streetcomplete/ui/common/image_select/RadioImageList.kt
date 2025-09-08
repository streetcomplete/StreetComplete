package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService
import de.westnordost.streetcomplete.quests.bicycle_repair_station.asItem
import de.westnordost.streetcomplete.view.image_select.DisplayItem

@Composable
fun <T> RadioImageList(items: List<DisplayItem<T>>,
    onSelect: (List<DisplayItem<T>>) -> Unit,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = 4,
    itemContent: @Composable (item: ImageListItem<T>, index: Int, onClick: () -> Unit, role: Role) -> Unit) {
    var listItems by remember { mutableStateOf(items.map { ImageListItem(it, false) }) }
    LaunchedEffect(items) {
        listItems = items.map { ImageListItem(it, false) }
    }
    ImageList(
        imageItems = listItems,
        onClick = { targetIndex, targetItem ->
            listItems = listItems.mapIndexed { currentIndex, currentItem ->
                if (targetIndex == currentIndex)
                    ImageListItem(currentItem.item, !currentItem.checked)
                else
                    ImageListItem(currentItem.item, false)
            }
            onSelect(listItems.filter { it.checked }.map { it.item })
        },
        itemRole = Role.RadioButton,
        modifier = modifier.fillMaxHeight(),
        itemsPerRow = itemsPerRow,
        itemContent = { item, index, onClick, role ->
            key(item.item to items) {
                itemContent(item, index, onClick, role)
            }
        }
    )
}

@Composable
@Preview(showBackground = true)
fun RadioImageListPreview() {
    val items = BicycleRepairStationService.entries.map { it.asItem() }
    RadioImageList(
        items = items,
        onSelect = {}
    ) { item, index, onClick, role ->
        SelectableImageItem(
            item = item.item,
            isSelected = item.checked,
            onClick = onClick,
            role = role
        )
    }
}
