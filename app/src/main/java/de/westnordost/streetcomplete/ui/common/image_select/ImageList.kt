package de.westnordost.streetcomplete.ui.common.image_select

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService
import de.westnordost.streetcomplete.quests.bicycle_repair_station.asItem
import de.westnordost.streetcomplete.view.image_select.Item

@Composable
fun <T> ImageList(imageItems: List<Item<T>>, itemsPerRow: Int = 4) {
    LazyVerticalGrid(columns = GridCells.Fixed(itemsPerRow),
        horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.height(400.dp)
        ) {
        itemsIndexed(imageItems) { index, item ->

            Box(contentAlignment = Alignment.BottomCenter) {
                Image(painter = painterResource(id = item.drawableId!!), contentDescription = "")
                Text(text = stringResource(item.titleId!!),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(5.0f, 10.0f),
                            blurRadius = 3f
                        )
                    ),
                    modifier = Modifier.padding(8.dp))
            }
        }
    }
}



@Composable
@Preview(showBackground = true)
fun ImageListPreview() {
    val items = BicycleRepairStationService.entries.map { it.asItem() }
    ImageList(imageItems = items, itemsPerRow = 3)
}
