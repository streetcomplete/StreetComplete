package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cancel
import de.westnordost.streetcomplete.resources.ok
import de.westnordost.streetcomplete.resources.quest_select_hint_most_specific
import de.westnordost.streetcomplete.ui.common.item_select.Group
import de.westnordost.streetcomplete.ui.common.item_select.GroupedItemSelect
import org.jetbrains.compose.resources.stringResource

/** Simple item select dialog, somewhat similar to SimpleListPickerDialog only that we have a grid
 *  of items here instead of a list of radio buttons. */
@Composable
fun <I, G: Group<I>> GroupedItemSelectDialog(
    onDismissRequest: () -> Unit,
    groups: List<G>,
    onSelected: (item: I) -> Unit,
    groupContent: @Composable (group: G) -> Unit,
    itemContent: @Composable (item: I) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(),
) {
    var selectedGroup by remember { mutableStateOf<G?>(null) }
    var selectedItem by remember { mutableStateOf<I?>(null) }
    val selected = selectedGroup?.item ?: selectedItem

    val scrollState = rememberScrollState()

    ScrollableAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(stringResource(Res.string.quest_select_hint_most_specific)) },
        content = {
            if (scrollState.canScrollBackward) Divider()
            GroupedItemSelect(
                groups = groups,
                topItems = emptyList(),
                selectedGroup = selectedGroup,
                selectedItem = selectedItem,
                onSelect = { group, item ->
                    selectedGroup = group
                    selectedItem = item
                },
                groupContent = groupContent,
                itemContent = itemContent,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .scrollable(scrollState, Orientation.Vertical)
            )
            if (scrollState.canScrollForward) Divider()
        },
        buttons = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
            TextButton(
                onClick = {
                    onDismissRequest()
                    selected?.let { onSelected(it) }
                },
                enabled = selected != null,
            ) {
                Text(stringResource(Res.string.ok))
            }
        },
        shape = shape,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        properties = properties,
    )


}
