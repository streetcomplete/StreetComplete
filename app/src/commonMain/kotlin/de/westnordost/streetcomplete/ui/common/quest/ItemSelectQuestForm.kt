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
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.stringResource

/** Quest form that lets the user select one item from a set of [items], displayed in a grid with a
 *  width of [itemsPerRow].
 *  If [moveFavoritesToFront] is true, moves the last picked items saved for [favoriteKey] to the
 *  front, but only if the items do not all fit into one line.
 *  */
@Composable
fun <I> ItemSelectQuestForm (
    items: List<I>,
    itemContent: @Composable (item: I) -> Unit,
    onClickOk: (item: I) -> Unit,
    prefs: Preferences,
    serializer: KSerializer<I>,
    favoriteKey: String,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = 4,
    moveFavoritesToFront: Boolean = true,
    otherAnswers: List<Answer> = emptyList(),
) {
    val reorderedItems = remember(items, itemsPerRow, moveFavoritesToFront) {
        if (items.size > itemsPerRow && moveFavoritesToFront) {
            val favourites = prefs
                .getLastPicked(ListSerializer(serializer), favoriteKey)
                .takeFavorites<I>(n = itemsPerRow)
            (favourites + items).distinct()
        } else {
            items
        }
    }
    var selectedItemIndex by rememberSaveable(items) { mutableStateOf(-1) }
    val selectedItem = selectedItemIndex.takeIf { it >= 0 }?.let { items[it] }

    QuestForm(
        answers = Confirm(
            isComplete = selectedItem != null,
            onClick = {
                prefs.addLastPicked(ListSerializer(serializer), favoriteKey, selectedItem!!)
                onClickOk(selectedItem)
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
                Text(stringResource(Res.string.quest_roofShape_select_one))
            }
            ItemSelectGrid(
                columns = SimpleGridCells.Fixed(itemsPerRow),
                items = reorderedItems,
                selectedItem = selectedItem,
                onSelect = { selectedItemIndex = items.indexOf(it) },
                modifier = Modifier.fillMaxWidth(),
                itemContent = itemContent
            )
        }
    }
}
