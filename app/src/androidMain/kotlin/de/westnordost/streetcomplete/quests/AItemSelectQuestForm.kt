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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a list of images and one to select.
 */
abstract class AItemSelectQuestForm<I, T> : AbstractOsmQuestForm<T>() {

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
        var selectedItem by rememberSerializable { mutableStateOf<I?>(null) }

        QuestForm(
            answers = Confirm(
                isComplete = selectedItem != null,
                onClick = {
                    prefs.addLastPicked(ListSerializer(serializer), this::class.simpleName!!, selectedItem!!)
                    onClickOk(selectedItem!!)
                }
            )
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
                    onSelect = { selectedItem = it },
                    modifier = Modifier.fillMaxWidth()
                ) { ItemContent(it) }
            }
        }
    }

    @Composable protected abstract fun ItemContent(item: I)

    protected abstract fun onClickOk(selectedItem: I)

    private fun moveFavouritesToFront(originalList: List<I>): List<I> {
        val favourites = prefs.getLastPicked(ListSerializer(serializer), this::class.simpleName!!)
            .takeFavorites(n = itemsPerRow)
        return (favourites + originalList).distinct()
    }
}
