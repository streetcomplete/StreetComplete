package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.quests.lanes.LanesType.MARKED
import de.westnordost.streetcomplete.quests.lanes.LanesType.MARKED_SIDES
import de.westnordost.streetcomplete.quests.lanes.LanesType.UNMARKED
import de.westnordost.streetcomplete.quests.lanes.Direction.FORWARD
import de.westnordost.streetcomplete.quests.lanes.Direction.BACKWARD
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.lanes_marked
import de.westnordost.streetcomplete.resources.lanes_marked_odd
import de.westnordost.streetcomplete.resources.lanes_unmarked
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_description2
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_description_one_side2
import de.westnordost.streetcomplete.resources.quest_lanes_answer_lanes_odd2
import de.westnordost.streetcomplete.resources.quest_lanes_answer_noLanes
import de.westnordost.streetcomplete.ui.common.dialogs.WheelPickerDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Two-step form to input how many lanes a road has */
@Composable
fun LanesForm(
    value: LanesAnswer?,
    onValueChanged: (LanesAnswer?) -> Unit,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
    tilt: Float = 0f,
    isOneway: Boolean = false,
    isLeftHandTraffic: Boolean = false,
    centerLineColor: Color = Color.White,
    edgeLineColor: Color = Color.White,
    edgeLineStyle: LineStyle = LineStyle.CONTINUOUS,

) {
    val selectableLanesTypes = remember(isOneway) {
        listOfNotNull(MARKED, UNMARKED, if (!isOneway) MARKED_SIDES else null)
    }

    var showTotalLanesSelect by remember { mutableStateOf(false) }
    var showLanesSelectForDirection by remember { mutableStateOf<Direction?>(null) }

    if (value is MarkedLanes) {
        // TODO rotate container
        LanesSelectPuzzle(
            laneCountForward = value.forward,
            laneCountBackward = value.backward,
            onClickForwardSide = { showLanesSelectForDirection = FORWARD },
            onClickBackwardSide = { showLanesSelectForDirection = BACKWARD },
            modifier = Modifier.fillMaxWidth().height(128.dp),
            centerLineColor = centerLineColor,
            edgeLineColor = edgeLineColor,
            edgeLineStyle = edgeLineStyle,
            hasCenterLeftTurnLane = value.centerLeftTurnLane,
            isLeftHandTraffic = isLeftHandTraffic,
        )
    } else {
        ItemSelectGrid(
            columns = SimpleGridCells.Fixed(3),
            items = selectableLanesTypes,
            selectedItem = if (value == UnmarkedLanes) UNMARKED else null,
            onSelect = { lanesType ->
                val newValue = when (lanesType) {
                    UNMARKED -> UnmarkedLanes
                    MARKED, MARKED_SIDES -> MarkedLanes()
                    null -> null
                }
                onValueChanged(newValue)

                // immediately ask for lanes if not odd number of lanes
                if (lanesType == MARKED) {
                    showTotalLanesSelect = true
                }
            },
        ) {
            val painter = painterResource(it.icon)
            ImageWithLabel(
                painter = remember(painter) { ClipCirclePainter(painter) } ,
                label = stringResource(it.title),
                imageRotation = rotation
            )
        }
    }

    if (showTotalLanesSelect) {
        val selectableLanes = remember { (2 .. 20 step 2).toList() }
        WheelPickerDialog(
            onDismissRequest = { showTotalLanesSelect = false },
            selectableValues = selectableLanes,
            onSelected = {
                val previousValue = (value as? MarkedLanes) ?: MarkedLanes()
                val newValue = previousValue.copy(forward = it / 2, backward = it / 2)
                onValueChanged(newValue)
            },
            itemContent = { Text(it.toString()) },
            selectedInitialValue = (value as? MarkedLanes)?.total,
            text = { Text(stringResource(Res.string.quest_lanes_answer_lanes_description2)) },
        )
    }

    showLanesSelectForDirection?.let { laneSelectDirection ->
        val selectableLanes = remember { (1 .. 10).toList() }
        WheelPickerDialog(
            onDismissRequest = { showLanesSelectForDirection = null },
            selectableValues = selectableLanes,
            onSelected = {
                val previousValue = (value as? MarkedLanes) ?: MarkedLanes()
                val newValue = when (laneSelectDirection) {
                    FORWARD -> previousValue.copy(forward = it)
                    BACKWARD -> previousValue.copy(backward = it)
                }
                onValueChanged(newValue)
            },
            itemContent = { Text(it.toString()) },
            selectedInitialValue = when (laneSelectDirection) {
                FORWARD -> (value as? MarkedLanes)?.forward
                BACKWARD -> (value as? MarkedLanes)?.backward
            },
            text = { Text(stringResource(Res.string.quest_lanes_answer_lanes_description_one_side2)) },
        )
    }
}

private enum class Direction { FORWARD, BACKWARD }

private enum class LanesType { MARKED, UNMARKED, MARKED_SIDES }

private val LanesType.icon: DrawableResource get() = when (this) {
    MARKED -> Res.drawable.lanes_marked
    UNMARKED -> Res.drawable.lanes_unmarked
    MARKED_SIDES -> Res.drawable.lanes_marked_odd
}

private val LanesType.title: StringResource get() = when (this) {
    MARKED -> Res.string.quest_lanes_answer_lanes
    UNMARKED -> Res.string.quest_lanes_answer_noLanes
    MARKED_SIDES -> Res.string.quest_lanes_answer_lanes_odd2
}
