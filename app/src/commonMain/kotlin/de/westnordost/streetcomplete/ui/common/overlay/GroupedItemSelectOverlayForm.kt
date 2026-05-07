package de.westnordost.streetcomplete.ui.common.overlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.ui.ItemCard
import de.westnordost.streetcomplete.ui.common.dialogs.GroupedItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.LocalElement
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.nameAndLocationLabel
import de.westnordost.streetcomplete.util.takeFavorites
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

/** Overlay form to select an item from a list of items grouped by [groups].
 *  It initially displays the [initialSelectedItem], clicking on it opens a dialog in which another
 *  item can be selected.
 *
 *  Additionally, previously picked items (persisted via [prefs] and [favoriteKey]) are displayed
 *  beneath that as chips, padded by [topSelectableItems], as a shortcut. */
@Composable
inline fun <reified G: Group<I>, reified I> GroupedItemSelectOverlayForm(
    groups: List<G>,
    topSelectableItems: List<I>,
    initialSelectedItem: I?,
    noinline groupContent: @Composable (group: G) -> Unit,
    noinline itemContent: @Composable (item: I) -> Unit,
    noinline lastPickedItemContent: @Composable (item: I) -> Unit,
    crossinline onClickOk: (selectedItem: I) -> Unit,
    prefs: Preferences,
    favoriteKey: String,
    modifier: Modifier = Modifier,
    label: AnnotatedString? = LocalElement.current?.let { element ->
        nameAndLocationLabel(element, featureDictionary)
    },
    otherAnswers: List<Answer> = emptyList(),
) {
    val lastPickedItems = remember {
        prefs.getLastPicked(ListSerializer(serializer<I>()), favoriteKey)
            .takeFavorites(n = 6, first = 1, pad = topSelectableItems)
    }
    var selectedItem by rememberSerializable { mutableStateOf(initialSelectedItem) }

    OverlayForm(
        isComplete = selectedItem != null,
        hasChanges = selectedItem != initialSelectedItem,
        onClickOk = {
            val value = selectedItem!!
            prefs.addLastPicked(ListSerializer(serializer()), favoriteKey, value)
            onClickOk(value)
        },
        modifier = modifier,
        label = label,
        otherAnswers = otherAnswers,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier.defaultMinSize(minHeight = 96.dp),
                contentAlignment = Alignment.Center,
            ) {
                var expanded by remember { mutableStateOf(false) }

                ItemCard(
                    item = selectedItem,
                    expanded = expanded,
                    onExpandChange = { expanded = it },
                    content = itemContent,
                )
                if (expanded) {
                    GroupedItemSelectDialog(
                        onDismissRequest = { expanded = false },
                        groups = groups,
                        onSelected = { selectedItem = it },
                        groupContent = groupContent,
                        itemContent = itemContent,
                    )
                }
            }
            if (lastPickedItems.isNotEmpty()) {
                LastPickedChipsRow(
                    items = lastPickedItems,
                    onClick = { selectedItem = it },
                    modifier = Modifier.padding(start = 48.dp, end = 56.dp),
                    itemContent = lastPickedItemContent
                )
            } else {
                Spacer(Modifier.size(48.dp))
            }
        }
    }
}
