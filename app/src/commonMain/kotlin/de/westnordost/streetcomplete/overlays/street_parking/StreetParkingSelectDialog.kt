package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
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
import de.westnordost.streetcomplete.osm.street_parking.dialogPainter
import de.westnordost.streetcomplete.osm.street_parking.painter
import de.westnordost.streetcomplete.osm.street_parking.title
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.select_street_parking_orientation
import de.westnordost.streetcomplete.resources.select_street_parking_position
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.item_select.ItemSelectGrid
import de.westnordost.streetcomplete.ui.ktx.fadingVerticalScrollEdges
import org.jetbrains.compose.resources.stringResource

/** Dialog in which both the parking orientation and parking position is selected in two steps. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun StreetParkingSelectionDialog(
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

    fun select(item: StreetParking) {
        onSelect(item)
        onDismissRequest()
    }

    fun selectStreetParkingSelection(selection: StreetParkingSelection) {
        when (selection) {
            StreetParkingSelection.PARALLEL -> parkingOrientation = ParkingOrientation.PARALLEL
            StreetParkingSelection.DIAGONAL -> parkingOrientation = ParkingOrientation.DIAGONAL
            StreetParkingSelection.PERPENDICULAR -> parkingOrientation = ParkingOrientation.PERPENDICULAR
            StreetParkingSelection.SEPARATE -> select(StreetParking.Separate)
            StreetParkingSelection.NO -> select(StreetParking.None)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        BackHandler(parkingOrientation != null) {
            parkingOrientation = null
        }

        Surface(
            modifier = modifier,
            shape = shape,
            color = backgroundColor,
            contentColor = contentColor,
        ) {
            AnimatedContent(
                targetState = parkingOrientation,
                transitionSpec = {
                    val dir = if (parkingOrientation != null) 1 else - 1
                    slideInHorizontally { it * dir } togetherWith slideOutHorizontally { -it * dir }
                }
            ) { orientation ->
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // title
                    CompositionLocalProvider(
                        LocalContentAlpha provides ContentAlpha.high,
                        LocalTextStyle provides MaterialTheme.typography.subtitle1
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp)
                        ) {
                            if (orientation == null) {
                                Text(stringResource(Res.string.select_street_parking_orientation))
                            } else {
                                IconButton(onClick = { parkingOrientation = null }) { BackIcon() }
                                Text(stringResource(Res.string.select_street_parking_position))
                            }
                        }
                    }
                    // select grid
                    val scrollState = rememberScrollState()
                    Box(Modifier
                        .fadingVerticalScrollEdges(scrollState, 32.dp)
                        .verticalScroll(scrollState),
                    ) {
                        if (orientation == null) {
                            StreetParkingSelectionSelectGrid(
                                isUpsideDown = isUpsideDown,
                                onSelect = ::selectStreetParkingSelection,
                            )
                        } else {
                            StreetParkingPositionSelectGrid(
                                orientation = orientation,
                                isUpsideDown = isUpsideDown,
                                onSelect = ::select,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Select the parking position */
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

/** Given a parking orientation, select the parking position */
@Composable private fun StreetParkingPositionSelectGrid(
    orientation: ParkingOrientation?,
    isUpsideDown: Boolean,
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
                painter = selection.dialogPainter(isUpsideDown),
                label = selection.title?.let { stringResource(it) },
            )
        },
        modifier = modifier,
    )
}
