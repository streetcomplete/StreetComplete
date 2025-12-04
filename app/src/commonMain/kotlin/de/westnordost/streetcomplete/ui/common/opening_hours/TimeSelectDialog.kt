package de.westnordost.streetcomplete.ui.common.opening_hours

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.opening_hours_no_fixed_end
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun TimeSelectDialog(
    onDismissRequest: () -> Unit,
    isOpenEnd: Boolean,
    modifier: Modifier = Modifier,
    title: (@Composable () -> Unit)? = null,
) {
    var openEnd by remember(isOpenEnd) { mutableStateOf(isOpenEnd) }

    ConfirmationDialog(
        onDismissRequest = onDismissRequest,
        onConfirmed = {},
        modifier = modifier,
        title = title,
        text = {
            // TODO

            Divider()

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.toggleable(openEnd) { openEnd = it }
            ) {
                Checkbox(
                    checked = openEnd,
                    onCheckedChange = { openEnd = it },
                )
                Text(stringResource(Res.string.opening_hours_no_fixed_end))
            }
        },
    )
}
