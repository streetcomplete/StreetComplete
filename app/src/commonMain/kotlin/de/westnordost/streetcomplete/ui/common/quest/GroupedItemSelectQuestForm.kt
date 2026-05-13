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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.item_select.GroupedItemSelectColumn
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/** Quest form that lets the user select one item from a set of items arranged in [groups].
 *
 *  At the top, ungrouped [topItems] are shown as a quick selection.
 *  If [favoriteKey] is not null, the last picked items saved for that key supplant the [topItems],
 *  i.e. are only padded by them. */
@Composable
inline fun <reified G: Group<I>, reified I> GroupedItemSelectQuestForm(
    groups: List<G>,
    topItems: List<I>,
    noinline groupContent: @Composable (group: G) -> Unit,
    noinline itemContent: @Composable (item: I) -> Unit,
    crossinline onClickOk: (selectedItem: I) -> Unit,
    modifier: Modifier = Modifier,
    favoriteKey: String? = null,
    otherAnswers: List<Answer> = emptyList(),
) {
    val viewModel = koinViewModel<ItemSelectViewModel>()

    val actualTopItems = remember(topItems) {
        if (favoriteKey != null) {
            viewModel.getTopItemsWithFavoritesFirst(favoriteKey, topItems)
        } else {
            topItems
        }
    }
    var selectedGroup by rememberSerializable { mutableStateOf<G?>(null) }
    var selectedItem by rememberSerializable { mutableStateOf<I?>(null) }

    var confirmSelectionOfGroupItem by remember { mutableStateOf<I?>(null) }

    QuestForm(
        answers = Form(
            isComplete = selectedItem != null || selectedGroup?.item != null,
            onClickOk = {
                val group = selectedGroup
                val groupItem = group?.item
                val item = selectedItem
                if (item != null) {
                    if (favoriteKey != null) {
                        viewModel.saveFavorite(favoriteKey, item)
                    }
                    onClickOk(item)
                }
                else if (groupItem != null) {
                    confirmSelectionOfGroupItem = groupItem
                }
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
                Text(stringResource(Res.string.quest_select_hint_most_specific))
            }
            GroupedItemSelectColumn(
                groups = groups,
                topItems = actualTopItems,
                selectedItem = selectedItem,
                selectedGroup = selectedGroup,
                onSelect = { group, item ->
                    selectedGroup = group
                    selectedItem = item
                },
                groupContent = groupContent,
                itemContent = itemContent,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    confirmSelectionOfGroupItem?.let { groupItem ->
        QuestConfirmationDialog(
            onDismissRequest = { confirmSelectionOfGroupItem = null },
            onConfirmed = {
                if (favoriteKey != null) {
                    viewModel.saveFavorite(favoriteKey, groupItem)
                }
                onClickOk(groupItem)
            },
            text = { Text(stringResource(Res.string.quest_generic_item_confirmation)) }
        )
    }
}
