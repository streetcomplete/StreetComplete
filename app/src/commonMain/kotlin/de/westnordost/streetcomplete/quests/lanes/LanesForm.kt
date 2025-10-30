package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.lanes.Direction.FORWARD
import de.westnordost.streetcomplete.quests.lanes.Direction.BACKWARD
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_description_one_side2
import de.westnordost.streetcomplete.ui.common.dialogs.WheelPickerDialog
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import de.westnordost.streetcomplete.ui.common.street_side_select.MiniCompass
import org.jetbrains.compose.resources.stringResource


/** Form to input how many lanes a road has */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LanesForm(
    value: Lanes,
    onValueChanged: (Lanes) -> Unit,
    modifier: Modifier = Modifier,
    wayRotation: Float = 0f,
    mapRotation: Float = 0f,
    mapTilt: Float = 0f,
    isOneway: Boolean = false,
    isReversedOneway: Boolean = false,
    isLeftHandTraffic: Boolean = false,
    centerLineColor: Color = Color.White,
    edgeLineColor: Color = Color.White,
    edgeLineStyle: LineStyle = LineStyle.CONTINUOUS,
) {
    var showPickerForDirection by remember { mutableStateOf<Direction?>(null) }

    val rotation = wayRotation - mapRotation

    Box(modifier = modifier
        .fillMaxWidth()
        .height(160.dp)
    ) {
        LanesSelect(
            value = value,
            onClickForwardSide = { showPickerForDirection = FORWARD },
            onClickBackwardSide = { showPickerForDirection = BACKWARD },
            modifier = Modifier.align(Alignment.Center),
            rotation = rotation,
            centerLineColor = centerLineColor,
            edgeLineColor = edgeLineColor,
            edgeLineStyle = edgeLineStyle,
            isLeftHandTraffic = isLeftHandTraffic,
            isOneway = isOneway,
            isReversedOneway = isReversedOneway
        )

        MiniCompass(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            rotation = -mapRotation,
            tilt = mapTilt
        )

        // just one quick-select button: 2 lanes covers most
        if (!isOneway && value.forward == null && value.backward == null) {
            LastPickedChipsRow(
                items = listOf(1),
                onClick = { onValueChanged(Lanes(it, it)) },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .align(Alignment.BottomStart),
                chipBorder = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
            ) { laneCount ->
                LanesButtonContent(
                    laneCount = laneCount,
                    rotation = rotation
                )
            }
        }
    }

    showPickerForDirection?.let { laneSelectDirection ->
        val selectableLanes = remember { (1 .. 10).toList() }
        WheelPickerDialog(
            onDismissRequest = { showPickerForDirection = null },
            selectableValues = selectableLanes,
            onSelected = {
                onValueChanged(when (laneSelectDirection) {
                    FORWARD -> value.copy(forward = it)
                    BACKWARD -> value.copy(backward = it)
                })
            },
            itemContent = { Text(it.toString()) },
            selectedInitialValue = when (laneSelectDirection) {
                FORWARD -> value.forward
                BACKWARD -> value.backward
            },
            text = { Text(stringResource(Res.string.quest_lanes_answer_lanes_description_one_side2)) },
        )
    }
}

private enum class Direction { FORWARD, BACKWARD }
