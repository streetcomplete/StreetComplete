package de.westnordost.streetcomplete.ui.common.item_select

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.ui.ktx.selectionFrame
import org.jetbrains.compose.resources.painterResource

/** List of grouped items where one item can be selected */
@Composable
fun <I, G: Group<I>> GroupedItemSelectColumn(
    groups: List<G>,
    topItems: List<I>,
    selectedGroup: G?,
    selectedItem: I?,
    onSelect: (group: G?, item: I?) -> Unit,
    groupContent: @Composable (group: G) -> Unit,
    itemContent: @Composable (item: I) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // top items first...
        for (item in topItems) {
            val isSelected = item == selectedItem
            Box(Modifier
                .fillMaxWidth()
                .selectionFrame(isSelected)
                .selectable(isSelected) { onSelect(null, if (isSelected) null else item) }
            ) {
                itemContent(item)
            }
        }
        // then the groups
        for (group in groups) {
            val isGroupSelected = group == selectedGroup && selectedItem == null
            val isGroupExpanded = group == selectedGroup
            Divider(thickness = 2.dp)
            Row(
                modifier = Modifier
                    .selectionFrame(isGroupSelected)
                    .selectable(isGroupSelected) {
                        onSelect(if (isGroupSelected) null else group, null)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.weight(1f)) {
                    groupContent(group)
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(8.dp)
                        .rotate(if (isGroupExpanded) 0f else 270f),
                )
            }
            // and the expanded group
            AnimatedVisibility(visible = isGroupExpanded) {
                Column {
                    for (item in group.children) {
                        val isItemSelected = item == selectedItem
                        Box(Modifier
                            .fillMaxWidth()
                            .padding(start = 40.dp)
                            .selectionFrame(isItemSelected)
                            .selectable(isItemSelected) {
                                if (isItemSelected) onSelect(null, null)
                                else onSelect(group, item)
                            }
                        ) {
                            itemContent(item)
                        }
                    }
                }
            }
        }
    }
}
