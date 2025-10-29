package de.westnordost.streetcomplete.quests.recycling_material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.resources.quest_recycling_materials_note
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

typealias RecyclingMaterialsItem = List<RecyclingMaterial>

/** Form to select the recycling materials in a container. This is a bit more involved, because
 *  clicking on certain items (plastic, glass) may open another dialog in which the user should
 *  select which kind of plastic. */
@Composable
fun RecyclingContainerMaterialsForm(
    items: List<RecyclingMaterialsItem>,
    selectedItems: Set<RecyclingMaterialsItem>,
    onSelectedItems: (Set<RecyclingMaterialsItem>) -> Unit,
    isAnyGlassRecyclable: Boolean,
    modifier: Modifier = Modifier,
) {
    var items by remember(items) { mutableStateOf(items) }
    var subItems by remember { mutableStateOf<List<RecyclingMaterialsItem>?>(null) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompositionLocalProvider(
            LocalContentAlpha provides ContentAlpha.medium,
            LocalTextStyle provides MaterialTheme.typography.body2
        ) {
            Text(stringResource(Res.string.quest_recycling_materials_note))
            Text(stringResource(Res.string.quest_multiselect_hint))
        }
        ItemsSelectGrid(
            columns = SimpleGridCells.Fixed(4),
            items = items,
            selectedItems = selectedItems,
            onSelect = { item, selected ->
                val currentSelectedItems = selectedItems.toMutableList()
                if (selected) {
                    if (item in RecyclingMaterial.selectablePlasticValues) {
                        subItems = RecyclingMaterial.selectablePlasticValues
                    } else if (isAnyGlassRecyclable && item in RecyclingMaterial.selectableGlassValues) {
                        subItems = RecyclingMaterial.selectableGlassValues
                    } else {
                        currentSelectedItems.add(item)
                        onSelectedItems(currentSelectedItems.toSet())
                    }
                } else {
                    currentSelectedItems.remove(item)
                    onSelectedItems(currentSelectedItems.toSet())
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            ImageWithLabel(painterResource(it.icon), stringResource(it.title))
        }
    }

    subItems?.let { subItems2 ->
        SimpleItemSelectDialog(
            onDismissRequest = { subItems = null },
            columns = SimpleGridCells.Fixed(3),
            items = subItems2,
            onSelected = { item ->
                items = items.map { if (it in subItems2) item else it }
                val currentSelectedItems = selectedItems.toMutableList()
                currentSelectedItems.add(item)
                onSelectedItems(currentSelectedItems.toSet())
            },
            itemContent = { item ->
                ImageWithLabel(painterResource(item.icon), stringResource(item.title))
            }
        )
    }
}
