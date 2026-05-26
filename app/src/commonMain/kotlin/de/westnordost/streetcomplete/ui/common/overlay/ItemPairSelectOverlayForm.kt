package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites

/** Similar to ItemSelectOverlayForm, but that there is actually a pair of Items. */
@Composable
inline fun <reified I> ItemPairSelectOverlayForm(
    itemsPerRow: Int,
    items: List<I>,
    initialSelectedItemPair: Pair<I?, I?>,
    noinline itemContent: @Composable (I) -> Unit,
    noinline lastPickedItemPairContent: @Composable (Pair<I, I>) -> Unit,
    crossinline onClickOk: (selectedItemPair: Pair<I, I>) -> Unit,
    labels: Pair<String, String>,
    prefs: Preferences,
    favoriteKey: String,
    modifier: Modifier = Modifier.Companion,
    otherAnswers: List<Answer> = emptyList(),
) {
    val lastPicked = remember {
        prefs.getLastPicked<Pair<I, I>>(favoriteKey).takeFavorites(n = 3, first = 1)
    }
    var selectedItemPair by rememberSerializable(initialSelectedItemPair) {
        mutableStateOf<Pair<I?, I?>>(initialSelectedItemPair)
    }

    var expandedIndex by remember { mutableIntStateOf(-1) }

    OverlayForm(
        isComplete = selectedItemPair.first != null && selectedItemPair.second != null,
        hasChanges = initialSelectedItemPair != selectedItemPair,
        onClickOk = {
            val value = Pair(selectedItemPair.first!!, selectedItemPair.second!!)
            prefs.addLastPicked(favoriteKey, value)
            onClickOk(value)
        },
        modifier = modifier,
        otherAnswers = otherAnswers
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.Center) {
                val labelList = labels.toList()
                selectedItemPair.toList().forEachIndexed { index, selectedItem ->
                    Column(
                        modifier = Modifier
                            .padding(4.dp)
                            .weight(1f, fill = false),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = labelList[index],
                            style = MaterialTheme.typography.caption.copy(
                                hyphens = Hyphens.Auto,
                                textAlign = TextAlign.Center,
                                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                            ),
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

            if (lastPicked.isNotEmpty()) {
                LastPickedChipsRow(
                    items = lastPicked,
                    onClick = { selectedItemPair = it },
                    modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                    itemContent = lastPickedItemPairContent
                )
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }
    }

    if (expandedIndex != -1) {
        SimpleItemSelectDialog(
            onDismissRequest = { expandedIndex = -1 },
            columns = SimpleGridCells.Fixed(itemsPerRow),
            items = items,
            onSelected = {
                selectedItemPair = Pair(
                    first = if (expandedIndex == 0) it else selectedItemPair.first,
                    second = if (expandedIndex == 1) it else selectedItemPair.second,
                )
            },
            itemContent = itemContent
        )
    }
}
