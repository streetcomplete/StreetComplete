package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import com.cheonjaeung.compose.grid.VerticalGrid
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService
import de.westnordost.streetcomplete.quests.bicycle_repair_station.asItem

@Composable
fun <T> ImageList(imageItems: List<ImageListItem<T>>,
    onClick: (index: Int, item: ImageListItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = 4,
    itemRole: Role = Role.Checkbox,
    itemContent: @Composable (item: ImageListItem<T>, index: Int, onClick: () -> Unit, role: Role) -> Unit) {
    VerticalGrid(
        columns = SimpleGridCells.Fixed(itemsPerRow),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        imageItems.forEachIndexed { index, item ->
            itemContent(item, index, { onClick(index, item) }, itemRole)
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ImageListPreview() {
    var items by remember {
        mutableStateOf(
            BicycleRepairStationService.entries.map { ImageListItem<BicycleRepairStationService>(it.asItem(), false) })  }
    ImageList(imageItems = items,
        onClick = { i, f ->
            items = items.mapIndexed { index, item -> if (index == i) ImageListItem(item.item, !item.checked) else item }
                  },
        itemsPerRow = 3) { item, index, onClick, role ->
        SelectableImageItem(
            item = item.item,
            isSelected = item.checked,
            onClick = onClick,
            role = role
        )
    }
}
