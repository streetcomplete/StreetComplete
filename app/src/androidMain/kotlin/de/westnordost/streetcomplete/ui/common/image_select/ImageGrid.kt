package de.westnordost.streetcomplete.ui.common.image_select

import android.content.res.Configuration
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService
import de.westnordost.streetcomplete.quests.bicycle_repair_station.asItem
import kotlin.math.max

@Composable
fun <T> ImageGrid(
    imageItems: List<ImageListItem<T>>,
    onClick: (index: Int, item: ImageListItem<T>) -> Unit,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = 4,
    itemRole: Role = Role.Checkbox,
    itemContent: @Composable (item: ImageListItem<T>, index: Int, onClick: () -> Unit, role: Role) -> Unit
) {
    Layout(
        modifier = modifier.selectableGroup(),
        content = {
            imageItems.forEachIndexed { index, item ->
                itemContent(item, index, { onClick(index, item) }, itemRole)
            }
        },
    ) { measurables, constraints ->

        if (measurables.isEmpty()) {
            return@Layout layout(constraints.maxWidth, 0) {}
        }
        val spacing = 8.dp.roundToPx()
        val itemWidth = (constraints.maxWidth - (itemsPerRow - 1) * spacing) / itemsPerRow

        // 1. Query intrinsic heights to find the max height of each row
        val rowCount = (measurables.size + itemsPerRow - 1) / itemsPerRow
        val rowHeights = IntArray(rowCount)
        measurables.forEachIndexed { index, measurable ->
            val rowIndex = index / itemsPerRow
            // Query the preferred height without performing a full measurement
            val height = measurable.minIntrinsicHeight(width = itemWidth)
            rowHeights[rowIndex] = max(rowHeights[rowIndex], height)
        }

        val totalHeight = rowHeights.sum() + (rowCount - 1).coerceAtLeast(0) * spacing

        // 2. Measure each item ONCE with final constraints and then place it
        layout(constraints.maxWidth, totalHeight) {
            val placeableItems = measurables.mapIndexed { index, measurable ->
                val rowIndex = index / itemsPerRow
                measurable.measure(
                    constraints.copy(
                        minWidth = itemWidth,
                        maxWidth = itemWidth,
                        minHeight = rowHeights[rowIndex],
                        maxHeight = rowHeights[rowIndex]
                    )
                )
            }
            var xPosition = 0
            var yPosition = 0
            var currentRowIndex = 0

            placeableItems.forEachIndexed { index, placeable ->
                val rowIndex = index / itemsPerRow

                if (rowIndex > currentRowIndex) {
                    yPosition += rowHeights[currentRowIndex] + spacing
                    xPosition = 0
                    currentRowIndex = rowIndex
                }

                placeable.placeRelative(xPosition, yPosition)

                xPosition += placeable.width + spacing
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun ImageGridPreview() {
    var items by remember {
        mutableStateOf(
            BicycleRepairStationService.entries.map { ImageListItem(it.asItem(), false) })  }

    ImageGrid(
        imageItems = items,
        onClick = { i, f ->
        items = items.mapIndexed { index, item -> if (index == i ) ImageListItem(item.item, !item.checked) else item }
    },
        itemsPerRow = 3
    ) { item, index, onClick, role ->
            SelectableImageCell(
                item = item.item,
                isSelected = item.checked,
                onClick = onClick,
                role = role)
    }
}


@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ImageGridDarkModePreview() {
    var items by remember {
        mutableStateOf(
            BicycleRepairStationService.entries.map { ImageListItem(it.asItem(), false) })  }

    ImageGrid(
        imageItems = items,
        onClick = { i, f ->
            items = items.mapIndexed { index, item -> if (index == i ) ImageListItem(item.item, !item.checked) else item }
        },
        itemsPerRow = 3
    ) { item, index, onClick, role ->
        SelectableImageCell(
            item = item.item,
            isSelected = item.checked,
            onClick = onClick,
            role = role)
    }
}
