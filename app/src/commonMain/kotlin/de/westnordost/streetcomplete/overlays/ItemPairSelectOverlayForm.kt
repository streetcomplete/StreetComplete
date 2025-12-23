package de.westnordost.streetcomplete.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow

/** Similar to ItemSelectOverlayForm, but that there is actually a pair of Items. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <I> ItemPairSelectOverlayForm(
    itemsPerRow: Int,
    items: List<I>,
    itemContent: @Composable (I) -> Unit,
    selectedItems: Pair<I?, I?>,
    lastPickedItemPair: List<Pair<I, I>>,
    lastPickedItemPairContent: @Composable (Pair<I, I>) -> Unit,
    onSelectItem: (Pair<I?, I?>) -> Unit,
    labels: Pair<String, String>,
    modifier: Modifier = Modifier,
) {
    val labelStyle = MaterialTheme.typography.caption.copy(
        hyphens = Hyphens.Auto,
        textAlign = TextAlign.Center,
        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
    )
    var expandedIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            val labelList = labels.toList()
            selectedItems.toList().forEachIndexed { index, selectedItem ->
                Column(
                    modifier = Modifier
                        .padding(4.dp)
                        .weight(1f, fill = false),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = labelList[index],
                        style = labelStyle,
                    )
                    Box(
                        modifier = Modifier.defaultMinSize(minHeight = 96.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ItemCard(
                            item = selectedItem,
                            expanded = expandedIndex == index,
                            onExpandChange = { expandedIndex = if (it) index else -1 },
                            content = itemContent,
                        )
                    }
                }
            }
        }

        if (expandedIndex != -1) {
            SimpleItemSelectDialog(
                onDismissRequest = { expandedIndex = -1 },
                columns = SimpleGridCells.Fixed(itemsPerRow),
                items = items,
                onSelected = {
                    onSelectItem(Pair(
                        first = if (expandedIndex == 0) it else selectedItems.first,
                        second = if (expandedIndex == 1) it else selectedItems.second,
                    ))
                },
                itemContent = itemContent
            )
        }

        if(lastPickedItemPair.isNotEmpty()) {
            LastPickedChipsRow(
                items = lastPickedItemPair,
                onClick = onSelectItem,
                modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                itemContent = lastPickedItemPairContent
            )
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}
