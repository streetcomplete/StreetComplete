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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.addLastPicked
import de.westnordost.streetcomplete.data.preferences.getLastPicked
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Quest form that lets the user select one item from a set of [items], displayed in a grid with a
 *  width of [itemsPerRow].
 *  If [favoriteKey] is not null, moves the last picked items saved for that key to the front in the
 *  first row.
 *  */
@Composable
inline fun <reified I> ItemSelectQuestForm(
    noinline on: (QuestAction<I>) -> Unit,
    items: List<I>,
    noinline itemContent: @Composable (item: I) -> Unit,
    modifier: Modifier = Modifier,
    itemsPerRow: Int = 3,
    favoriteKey: String? = null,
    title: String = stringResource(LocalQuestType.current!!.title),
    noinline otherAnswers: @Composable (() -> List<AnswerItem>) = { emptyList() },
    preferences: Preferences = koinInject()
) {
    val reorderedItems = remember(items, itemsPerRow, favoriteKey) {
        if (favoriteKey != null) {
            val favourites = preferences.getLastPicked<I>(favoriteKey)
                .takeFavorites(n = itemsPerRow)
            (favourites + items).distinct()
        } else {
            items
        }
    }
    var selectedItem by rememberSerializable { mutableStateOf<I?>(null) }

    QuestForm(
        on = on,
        isComplete = selectedItem != null,
        onClickOk = {
            val value = selectedItem!!
            if (favoriteKey != null) {
                preferences.addLastPicked(favoriteKey, value)
            }
            on(Answer(value))
        },
        modifier = modifier,
        title = title,
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
                onSelect = { selectedItem = it },
                modifier = Modifier.fillMaxWidth(),
                itemContent = itemContent
            )
        }
    }
}
