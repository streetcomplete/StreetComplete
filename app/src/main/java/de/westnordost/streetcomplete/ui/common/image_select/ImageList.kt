package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService
import de.westnordost.streetcomplete.quests.bicycle_repair_station.asItem

@Composable
fun <T> ImageList(imageItems: List<ImageListItem<T>>, onClick: (index: Int, item: ImageListItem<T>) -> Unit, modifier: Modifier = Modifier, itemsPerRow: Int = 4) {
    LazyVerticalGrid(columns = GridCells.Fixed(itemsPerRow),
        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.height(400.dp)
        ) {
        itemsIndexed(imageItems) { index, item ->
            SelectableImageItem(
                imageResId = item.item.drawableId!!,
                title = stringResource(item.item.titleId!!),
                isSelected = item.checked,
                onClick = { onClick(index, item) }
                )
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
        itemsPerRow = 3)
}
