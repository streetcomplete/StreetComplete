package de.westnordost.streetcomplete.ui.common.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.ui.common.DropdownMenuItem
import org.jetbrains.compose.resources.painterResource

/** A select for use in a settings screen. (See also SelectButton, which is different insofar as
 *  it does not hoist the expanded state and that it is a button while this should be used inside
 *  a Preference because the Preference already behaves like a button) */
@Composable
fun <T> Select(
    items: List<T>,
    selectedItem: T,
    onSelected: (T) -> Unit,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable RowScope.(item: T) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        itemContent(selectedItem)
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_drop_down_24),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 8.dp)
                .rotate(if (expanded) 180f else 0f)
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
        ) {
            for (item in items) {
                DropdownMenuItem(onClick = {
                    onSelected(item)
                    onDismissRequest()
                }) {
                    itemContent(item)
                }
            }
        }
    }
}
