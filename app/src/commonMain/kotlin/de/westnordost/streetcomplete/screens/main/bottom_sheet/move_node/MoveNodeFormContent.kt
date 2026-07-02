package de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.theme.titleLarge
import org.jetbrains.compose.resources.stringResource

/** Bottom sheet content for the move node UI */
@Composable
fun MoveNodeFormContent(
    distance: Double,
    displayUnit: MeasureDisplayUnit,
    onClickCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = when {
                    distance < MIN_MOVE_DISTANCE ->
                        stringResource(Res.string.node_moved_not_far_enough)
                    distance > MAX_MOVE_DISTANCE ->
                        stringResource(Res.string.node_moved_too_far)
                    else ->
                        stringResource(Res.string.node_moved, displayUnit.format(distance.toFloat()))
                },
                style = MaterialTheme.typography.titleLarge,
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = stringResource(Res.string.move_node_description),
                    style = MaterialTheme.typography.body2,
                )
            }
        }

        Divider()

        // button panel
        Column(Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
        ) {
            TextButton(onClickCancel) { Text(stringResource(Res.string.cancel)) }
        }
    }
}
