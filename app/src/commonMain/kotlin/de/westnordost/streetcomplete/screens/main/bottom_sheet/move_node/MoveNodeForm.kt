package de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.theme.titleLarge
import org.jetbrains.compose.resources.stringResource

// Require a minimum distance because the map is not perfectly precise, it may be hard to tell
// whether something really is misplaced without good aerial imagery.
// Also, POIs are objects with a certain extent, so as long as the node is within this extent, it's
// fine, there is little value of putting the point at exactly the center point of the POI
const val MIN_MOVE_DISTANCE = 1.0
// Move node functionality is meant for fixing slightly misplaced elements. If something moved far
// away, it is reasonable to assume there are more substantial changes required, also to nearby
// elements. Additionally, the default radius for highlighted elements is 30 m, so moving outside
// should not be allowed.
const val MAX_MOVE_DISTANCE = 30.0

/** Bottom sheet content for the move node UI */
@Composable
fun MoveNodeForm(
    distance: Float,
    displayUnit: MeasureDisplayUnit,
    onClickCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = when {
                distance < MIN_MOVE_DISTANCE ->
                    stringResource(Res.string.node_moved_not_far_enough)
                distance > MAX_MOVE_DISTANCE ->
                    stringResource(Res.string.node_moved_too_far)
                else ->
                    stringResource(Res.string.node_moved, displayUnit.format(distance))
            },
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(Res.string.move_node_description),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.alpha(ContentAlpha.medium)
        )

        Divider()

        // button panel
        Column(Modifier.fillMaxWidth()) {
            TextButton(onClickCancel) { Text(stringResource(Res.string.cancel)) }
        }
    }
}
