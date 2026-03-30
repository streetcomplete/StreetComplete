package de.westnordost.streetcomplete.screens.main.bottom_sheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.theme.titleLarge
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The three possible distance states when moving a node */
sealed interface MoveNodeDistanceState {
    data object TooClose : MoveNodeDistanceState
    data object TooFar : MoveNodeDistanceState
    data class InRange(val formattedDistance: String) : MoveNodeDistanceState
}

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
    distanceState: MoveNodeDistanceState,
    onClickOk: () -> Unit,
    onClickCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column {
            // content area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 24.dp)
            ) {
                Text(
                    text = when (distanceState) {
                        is MoveNodeDistanceState.TooClose ->
                            stringResource(Res.string.node_moved_not_far_enough)
                        is MoveNodeDistanceState.TooFar ->
                            stringResource(Res.string.node_moved_too_far)
                        is MoveNodeDistanceState.InRange ->
                            stringResource(Res.string.node_moved, distanceState.formattedDistance)
                    },
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(Res.string.move_node_description),
                )
            }

            Divider()

            // button panel
            Row(modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onClickCancel) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        }

        // floating OK button
        AnimatedVisibility(
            visible = distanceState is MoveNodeDistanceState.InRange,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
        ) {
            FloatingActionButton(
                onClick = onClickOk,
                shape = CircleShape,
                backgroundColor = MaterialTheme.colors.secondary,
                modifier = Modifier.size(72.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check_48dp),
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSecondary,
                )
            }
        }
    }
}
