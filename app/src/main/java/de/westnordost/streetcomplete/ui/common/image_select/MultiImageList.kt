package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService
import de.westnordost.streetcomplete.quests.bicycle_repair_station.asItem
import de.westnordost.streetcomplete.view.image_select.Item

@Composable
fun <T> MultiImageList(items: List<Item<T>>, onSelect: (List<Item<T>>) -> Unit, modifier: Modifier = Modifier) {
    var listItems by remember { mutableStateOf(items.map { ImageListItem(it, false) }) }
    ImageList<T>(
        imageItems = listItems,
        onClick = { targetIndex, targetItem ->
            listItems = listItems.mapIndexed { currentIndex, currentItem -> if (targetIndex == currentIndex) ImageListItem(currentItem.item, !currentItem.checked) else currentItem }
            onSelect(listItems.filter { it.checked }.map { it.item })
        },
        modifier = modifier
    )

}
@Composable
@Preview(showBackground = true)
fun MultiImageListPreview() {
    val items = BicycleRepairStationService.entries.map { it.asItem() }
    MultiImageList(
        items = items,
        onSelect = { selected ->
        }
    )
}
