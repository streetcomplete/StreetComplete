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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.resources.quest_recycling_materials_note
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.util.tree.Node
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to select the recycling materials in a container. This is a bit more involved, because
 *  certain values are actually sub-categories of other values. */
@Composable
fun RecyclingContainerMaterialsForm(
    items: List<RecyclingMaterial>,
    tree: Node<RecyclingMaterial>,
    selectedItems: Set<RecyclingMaterial>,
    onSelectedItems: (Set<RecyclingMaterial>) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                if (!selected) {
                    onSelectedItems(selectedItems - item)
                } else {
                    val selectedItems = selectedItems.toMutableSet()
                    val parentItems = tree.yieldParentValues(item).orEmpty()
                    val childItems = tree.yieldChildValues(item).orEmpty()
                    selectedItems.removeAll(parentItems)
                    selectedItems.removeAll(childItems)
                    selectedItems.add(item)
                    onSelectedItems(selectedItems)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            ImageWithLabel(painterResource(it.icon), stringResource(it.title))
        }
    }
}
