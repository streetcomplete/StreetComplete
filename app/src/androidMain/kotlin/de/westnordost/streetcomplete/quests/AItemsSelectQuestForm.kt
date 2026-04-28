package de.westnordost.streetcomplete.quests

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_multiselect_hint
import de.westnordost.streetcomplete.ui.common.item_select.ItemsSelectGrid
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a list of images and several to select.
 */
abstract class AItemsSelectQuestForm<I, T> : AbstractOsmQuestForm<T>() {

    private val prefs: Preferences by inject()

    protected open val itemsPerRow = 4

    /** return true to move last picked items to the front. On by default. Only respected if the
     *  items do not all fit into one line */
    protected open val moveFavoritesToFront = true
    /** items to display. May not be accessed before onCreate */
    protected abstract val items: List<I>

    protected abstract val serializer: KSerializer<I>

    @Composable
    override fun Content() {
        val reorderedItems = remember {
            if (items.size > itemsPerRow && moveFavoritesToFront) {
                moveFavouritesToFront(items)
            } else items
        }
        var selectedItems by rememberSerializable { emptySet<I>() }

        QuestForm(
            answers = Confirm(
                isComplete = selectedItems.isNotEmpty(),
                onClick = {
                    val values = selectedItems
                    prefs.addLastPicked(ListSerializer(serializer), this::class.simpleName!!, values.toList())
                    onClickOk(values)
                }
            )
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
                        selectedItems =
                            if (selected) { selectedItems + item }
                            else { selectedItems - item }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { ItemContent(it) }
            }
        }
    }

    @Composable protected abstract fun ItemContent(item: I)

    protected abstract fun onClickOk(selectedItems: Set<I>)

    private fun moveFavouritesToFront(originalList: List<I>): List<I> {
        val favourites = prefs.getLastPicked(ListSerializer(serializer), this::class.simpleName!!)
            .takeFavorites(n = itemsPerRow)
        return (favourites + originalList).distinct()
    }
}
