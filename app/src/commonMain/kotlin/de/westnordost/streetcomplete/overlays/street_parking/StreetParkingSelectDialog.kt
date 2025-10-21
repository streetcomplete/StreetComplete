package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.StreetParkingSelection
import de.westnordost.streetcomplete.osm.street_parking.painter
import de.westnordost.streetcomplete.osm.street_parking.title
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.select_street_parking_orientation
import de.westnordost.streetcomplete.resources.select_street_parking_position
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.dialogs.AlertDialogLayout
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.common.street_side_select.Side
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import org.jetbrains.compose.resources.stringResource

@Composable
fun StreetParkingSelectionDialog(
    side: Side,
    isUpsideDown: Boolean,
    onDismissRequest: () -> Unit,
    onSelect: (item: StreetParking) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    properties: DialogProperties = DialogProperties(),
) {
    var parkingOrientation by remember { mutableStateOf<ParkingOrientation?>(null) }
    val hasParkingOrientation by remember { derivedStateOf { parkingOrientation != null } }

    fun selectStreetParkingSelection(selection: StreetParkingSelection) {
        when (selection) {
            StreetParkingSelection.PARALLEL -> parkingOrientation = ParkingOrientation.PARALLEL
            StreetParkingSelection.DIAGONAL -> parkingOrientation = ParkingOrientation.DIAGONAL
            StreetParkingSelection.PERPENDICULAR -> parkingOrientation = ParkingOrientation.PERPENDICULAR
            StreetParkingSelection.SEPARATE -> onSelect(StreetParking.Separate)
            StreetParkingSelection.NO -> onSelect(StreetParking.None)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        AlertDialogLayout(
            modifier = modifier,
            title = {
                AnimatedContent(
                    targetState = hasParkingOrientation,
                ) { hasParkingOrientation ->
                    if (hasParkingOrientation) {
                        Row {
                            IconButton(onClick = { parkingOrientation = null }) { BackIcon() }
                            Text(stringResource(Res.string.select_street_parking_position))
                        }
                    } else {
                        Text(stringResource(Res.string.select_street_parking_orientation))
                    }
                }
            },
            content = {
                AnimatedContent(
                    targetState = hasParkingOrientation,
                ) { hasParkingOrientation ->
                    if (hasParkingOrientation) {
                        val scrollState = rememberScrollState()
                        Box(Modifier
                            .fadingVerticalScrollEdges(scrollState, 64.dp)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp),
                        ) {
                            StreetParkingPositionSelectGrid(
                                orientation = parkingOrientation,
                                isUpsideDown = isUpsideDown,
                                isRightSide = side == Side.RIGHT,
                                onSelect = onSelect,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    } else {
                        val scrollState = rememberScrollState()
                        Box(Modifier
                            .fadingVerticalScrollEdges(scrollState, 64.dp)
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp),
                        ) {
                            StreetParkingSelectionSelectGrid(
                                isUpsideDown = isUpsideDown,
                                onSelect = ::selectStreetParkingSelection,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                        }
                    }
                }
            },
            shape = shape,
            backgroundColor = backgroundColor,
            contentColor = contentColor
        )
    }
}

@Composable private fun StreetParkingSelectionSelectGrid(
    isUpsideDown: Boolean,
    onSelect: (StreetParkingSelection) -> Unit,
    modifier: Modifier = Modifier,
) {
    ItemSelectGrid(
        columns = SimpleGridCells.Fixed(2),
        items = StreetParkingSelection.entries,
        selectedItem = null,
        onSelect = { if (it != null) onSelect(it) },
        itemContent = { selection ->
            ImageWithLabel(
                painter = selection.painter(isUpsideDown),
                label = stringResource(selection.title),
            )
        },
        modifier = modifier
    )
}

@Composable private fun StreetParkingPositionSelectGrid(
    orientation: ParkingOrientation?,
    isUpsideDown: Boolean,
    isRightSide: Boolean,
    onSelect: (StreetParking.PositionAndOrientation) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (orientation == null) return

    val parkingPositionAndOrientations = remember(orientation) {
        ParkingPosition.displayedValues.map { StreetParking.PositionAndOrientation(orientation, it) }
    }

    ItemSelectGrid(
        columns = SimpleGridCells.Fixed(2),
        items = parkingPositionAndOrientations,
        selectedItem = null,
        onSelect = { if (it != null) onSelect(it) },
        itemContent = { selection ->
            ImageWithLabel(
                painter = selection.painter(isUpsideDown, isRightSide),
                label = selection.title?.let { stringResource(it) },
            )
        },
        modifier = modifier,
    )
}
