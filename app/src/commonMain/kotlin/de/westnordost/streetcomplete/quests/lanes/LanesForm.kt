package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.quests.lanes.Direction.FORWARD
import de.westnordost.streetcomplete.quests.lanes.Direction.BACKWARD
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_description_one_side2
import de.westnordost.streetcomplete.ui.common.dialogs.WheelPickerDialog
import org.jetbrains.compose.resources.stringResource

/** Form to input how many lanes a road has */
@Composable
fun LanesForm(
    value: Lanes,
    onValueChanged: (Lanes) -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    tilt: Float = 0f,
    isOneway: Boolean = false,
    isReversedOneway: Boolean = false,
    isLeftHandTraffic: Boolean = false,
    centerLineColor: Color = Color.White,
    edgeLineColor: Color = Color.White,
    edgeLineStyle: LineStyle = LineStyle.CONTINUOUS,
) {
    var showPickerForDirection by remember { mutableStateOf<Direction?>(null) }

    // TODO rotate container

    // TODO oneway + reversed oneway
    val laneCountForward = if (!isReversedOneway) value.forward else 0
    val laneCountBackward = if (!isOneway || isReversedOneway) value.backward else 0

    LanesSelectPuzzle(
        laneCountForward = laneCountForward,
        laneCountBackward = laneCountBackward,
        onClickForwardSide = { showPickerForDirection = FORWARD },
        onClickBackwardSide = { showPickerForDirection = BACKWARD },
        modifier = modifier.fillMaxWidth().height(128.dp),
        centerLineColor = centerLineColor,
        edgeLineColor = edgeLineColor,
        edgeLineStyle = edgeLineStyle,
        hasCenterLeftTurnLane = value.centerLeftTurnLane,
        isLeftHandTraffic = isLeftHandTraffic,
    )

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
