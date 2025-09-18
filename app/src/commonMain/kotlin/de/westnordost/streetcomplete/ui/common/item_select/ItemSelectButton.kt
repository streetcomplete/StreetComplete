package de.westnordost.streetcomplete.ui.common.item_select

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.resources.quest_select_hint
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.ktx.minus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Similar to the DropdownButton, only that it opens a SimpleItemSelectDialog instead of a dropdown
 *  menu */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <I> ItemSelectButton(
    columns: SimpleGridCells,
    items: List<I>,
    onSelected: (I) -> Unit,
    modifier: Modifier = Modifier,
    selectedItem: I? = null,
    itemContent: @Composable ((item: I) -> Unit),
    content: @Composable (() -> Unit) = {
        if (selectedItem != null) itemContent(selectedItem)
        else Text(stringResource(Res.string.quest_select_hint))
    }
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { expanded = !expanded },
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        border = ButtonDefaults.outlinedBorder,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(ButtonDefaults.ContentPadding - PaddingValues(end = 8.dp))
        ) {
            content()
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .rotate(if (expanded) 180f else 0f)
            )
        }

    }
    if (expanded) {
        SimpleItemSelectDialog(
            onDismissRequest = { expanded = false },
            columns = columns,
            items = items,
            onSelected = { onSelected(it) },
            itemContent = itemContent
        )
    }
}
