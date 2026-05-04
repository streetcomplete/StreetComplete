package de.westnordost.streetcomplete.ui.common.quest

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.stringResource

/** Quest form that lets the user select several items from a set of [items], displayed in a grid
 *  with a width of [itemsPerRow].
 *  If [moveFavoritesToFront] is true, moves the last picked items saved for [favoriteKey] to the
 *  front, but only if the items do not all fit into one line.
 *  */
@Composable
inline fun <reified I> ItemsSelectQuestForm(
    items: List<I>,
    itemsPerRow: Int,
    noinline itemContent: @Composable (item: I) -> Unit,
    crossinline onClickOk: (selectedItems: Set<I>) -> Unit,
    prefs: Preferences,
    favoriteKey: String,
    modifier: Modifier = Modifier,
    moveFavoritesToFront: Boolean = true,
    otherAnswers: List<Answer> = emptyList(),
) {
    val reorderedItems = remember(items, itemsPerRow, moveFavoritesToFront) {
        if (items.size > itemsPerRow && moveFavoritesToFront) {
            val favourites = prefs
                .getLastPicked(ListSerializer(serializer<I>()), favoriteKey)
                .takeFavorites<I>(n = itemsPerRow)
                .filter { it in items } // only those actually in items
            (favourites + items).distinct()
        } else {
            items
        }
    }
    var selectedItemIndices by rememberSaveable(items) { mutableStateOf<Set<Int>>(emptySet()) }
    val selectedItems by remember {
        derivedStateOf { selectedItemIndices.mapTo(HashSet()) { items[it] } }
    }

    QuestForm(
        answers = Confirm(
            isComplete = selectedItems.isNotEmpty(),
            onClick = {
                prefs.addLastPicked(ListSerializer(serializer<I>()), favoriteKey, selectedItems.toList())
                onClickOk(selectedItems)
            }
        ),
        modifier = modifier,
        otherAnswers = otherAnswers,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.medium,
                LocalTextStyle provides MaterialTheme.typography.body2
            ) {
                Text(stringResource(Res.string.quest_multiselect_hint))
            }
            ItemsSelectGrid(
                columns = SimpleGridCells.Fixed(itemsPerRow),
                items = reorderedItems,
                selectedItems = selectedItems,
                onSelect = { item, selected ->
                    val itemIndex = items.indexOf(item)
                    if (selected) {
                        selectedItemIndices += itemIndex
                    } else {
                        selectedItemIndices -= itemIndex
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                itemContent = itemContent
            )
        }
    }
}
