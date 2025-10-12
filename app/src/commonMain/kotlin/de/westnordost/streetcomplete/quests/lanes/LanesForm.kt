package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import de.westnordost.streetcomplete.quests.lanes.Direction.FORWARD
import de.westnordost.streetcomplete.quests.lanes.Direction.BACKWARD
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.compass
import de.westnordost.streetcomplete.resources.compass_needle_48
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_description_one_side2
import de.westnordost.streetcomplete.ui.common.dialogs.WheelPickerDialog
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos


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
    val scale = 1f + abs(cos(rotation * PI / 180)).toFloat() * 0.67f

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        LanesSelect(
            value = value,
            onClickForwardSide = { showPickerForDirection = FORWARD },
            onClickBackwardSide = { showPickerForDirection = BACKWARD },
            modifier = Modifier
                .align(Alignment.Center)
                .requiredWidth(min(maxWidth, maxHeight))
                .requiredHeight(max(maxWidth, maxHeight))
                .rotate(rotation)
                .scale(scale),
            centerLineColor = centerLineColor,
            edgeLineColor = edgeLineColor,
            edgeLineStyle = edgeLineStyle,
            isLeftHandTraffic = isLeftHandTraffic,
            isOneway = isOneway,
            isReversedOneway = isReversedOneway
        )

        Image(
            painter = painterResource(Res.drawable.compass_needle_48),
            contentDescription = stringResource(Res.string.compass),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .border(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f), CircleShape)
                .background(MaterialTheme.colors.surface, CircleShape)
                .padding(4.dp)
                .size(24.dp)
                .graphicsLayer(
                    rotationZ = -mapRotation,
                    rotationX = mapTilt
                )
        )

        // just one quick-select button: 2 lanes covers most
        if (!isOneway && value.forward == null && value.backward == null) {
            LastPickedChipsRow(
                items = listOf(1),
                onClick = { onValueChanged(Lanes(it, it)) },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                chipBorder = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
            ) {
                LanesButtonContent(lanes = it, rotation = rotation)
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
