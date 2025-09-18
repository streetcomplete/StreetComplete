package de.westnordost.streetcomplete.ui.common.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_select_hint
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import org.jetbrains.compose.resources.stringResource

/** Simple item select dialog, somewhat similar to SimpleListPickerDialog only that we have a grid
 *  of items here instead of a list of radio buttons. */
@Composable
fun <I> SimpleItemSelectDialog(
    onDismissRequest: () -> Unit,
    columns: SimpleGridCells,
    items: List<I>,
    onSelected: (item: I) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable (item: I) -> Unit,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(),
) {
    val scrollState = rememberScrollState()

    fun select(item: I) {
        onDismissRequest()
        onSelected(item)
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        AlertDialogLayout(
            modifier = modifier,
            title = { Text(stringResource(Res.string.quest_select_hint)) },
            content = {
                Column(Modifier
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                ) {
                    ItemSelectGrid(
                        columns = columns,
                        items = items,
                        selectedItem = null,
                        onSelect = { if (it != null) select(it) },
                        itemContent = itemContent,
                    )
                }
            },
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor
        )
    }
}
