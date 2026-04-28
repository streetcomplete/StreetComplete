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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.item_select.GroupedItemSelectColumn
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject

/**
 * Abstract class for quests with a grouped list of images and one to select.
 */
abstract class AGroupedItemSelectQuestForm<G: Group<I>, I, T> : AbstractOsmQuestForm<T>() {

    private val prefs: Preferences by inject()

    /** all items to display. May not be accessed before onCreate */
    protected abstract val groups: List<G>
    /** items to display that are shown on the top. May not be accessed before onCreate */
    protected abstract val topItems: List<I>
    private lateinit var actualTopItems: List<I>

    protected abstract val serializer: KSerializer<I>

    @Composable
    override fun Content() {
        val actualTopItems = remember {
            prefs.getLastPicked(ListSerializer(serializer), this::class.simpleName!!)
                .takeFavorites(n = 6, first = 1, pad = topItems)
        }
        var selectedGroup by rememberSerializable { mutableStateOf<G?>(null) }
        var selectedItem by rememberSerializable { mutableStateOf<I?>(null) }

        var confirmSelectionOfGroupItem by remember { mutableStateOf<I?>(null) }

        QuestForm(
            answers = Confirm(
                isComplete = selectedItem != null || selectedGroup?.item != null,
                onClick = {
                    val group = selectedGroup
                    val groupItem = group?.item
                    val item = selectedItem
                    if (item != null) {
                        prefs.addLastPicked(ListSerializer(serializer), this::class.simpleName!!, item)
                        onClickOk(item)
                    }
                    else if (groupItem != null) {
                        confirmSelectionOfGroupItem = groupItem
                    }
                }
            ),
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
                    groupContent = { GroupContent(it) },
                    itemContent = { ItemContent(it) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        confirmSelectionOfGroupItem?.let { groupItem ->
            QuestConfirmationDialog(
                onDismissRequest = { confirmSelectionOfGroupItem = null },
                onConfirmed = {
                    prefs.addLastPicked(ListSerializer(serializer), this::class.simpleName!!, groupItem)
                    onClickOk(groupItem)
                },
                text = { Text(stringResource(Res.string.quest_generic_item_confirmation)) }
            )
        }
    }

    @Composable protected abstract fun GroupContent(item: G)

    @Composable protected abstract fun ItemContent(item: I)

    abstract fun onClickOk(value: I)
}
