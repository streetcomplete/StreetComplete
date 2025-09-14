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
import androidx.compose.runtime.remember
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
fun <I, G: Group<I>> GroupedItemSelect(
    groups: List<G>,
    topItems: List<I>,
    selectedItem: I?,
    selectedGroup: G?,
    onSelect: (group: G?, item: I?) -> Unit,
    modifier: Modifier = Modifier,
    groupContent: @Composable (group: G) -> Unit,
    itemContent: @Composable (item: I) -> Unit,
) {
    // to know which item belongs to which group
    val itemGroup = remember(groups) {
        val map = HashMap<I, G>()
        for (group in groups) {
            for (item in group.items) {
                map.put(item, group)
            }
        }
        return@remember map
    }
    val expandedGroup = selectedGroup ?: selectedItem?.let { itemGroup[it] }

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
            val isSelected = group == selectedGroup
            val isExpanded = group == expandedGroup
            Divider(thickness = 2.dp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier
                    .weight(1f)
                    .selectionFrame(isSelected)
                    .selectable(isSelected) { onSelect(if (isSelected) null else group, null) }
                ) {
                    groupContent(group)
                }
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp).rotate(if (isExpanded) 0f else 270f),
                )
            }
            // and the expanded group
            AnimatedVisibility(visible = isExpanded) {
                for (item in group.items) {
                    Box(Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp)
                        .selectionFrame(isSelected)
                        .selectable(isSelected) { onSelect(null, if (isSelected) null else item) }
                    ) {
                        itemContent(item)
                    }
                }
            }
        }
    }
}
