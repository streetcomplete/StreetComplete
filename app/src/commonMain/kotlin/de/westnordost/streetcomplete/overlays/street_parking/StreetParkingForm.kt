package de.westnordost.streetcomplete.overlays.street_parking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.street_parking.StreetParking
import de.westnordost.streetcomplete.osm.street_parking.floatingIcon
import de.westnordost.streetcomplete.osm.street_parking.painter
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.street_parking_street_width
import de.westnordost.streetcomplete.resources.street_side_unknown
import de.westnordost.streetcomplete.resources.street_side_unknown_l
import de.westnordost.streetcomplete.ui.common.TextWithHalo
import de.westnordost.streetcomplete.ui.common.street_side_select.Side
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to input the street parking situation. */
@Composable fun StreetParkingForm(
    value: Sides<StreetParking>,
    onValueChanged: (Sides<StreetParking>) -> Unit,
    width: String?,
    geometryRotation: Float,
    mapRotation: Float,
    mapTilt: Float,
    isLeftHandTraffic: Boolean,
    isForwardOneway: Boolean,
    isReversedOneway: Boolean,
    modifier: Modifier = Modifier,
    lastPicked: List<Sides<StreetParking>> = emptyList(),
) {
    var showPickerForSide by remember { mutableStateOf<Side?>(null) }

    fun isUpsideDown(side: Side) = when (side) {
        Side.LEFT -> !isReversedOneway && (isForwardOneway || isLeftHandTraffic)
        Side.RIGHT -> !isForwardOneway && (isReversedOneway || isLeftHandTraffic)
    }

    Box {
        StreetSideForm(
            value = value,
            onValueChanged = onValueChanged,
            getIllustrationPainter = { parking, side ->
                val isUpsideDown = isUpsideDown(side)
                val defaultPainter = painterResource(
                    if (isUpsideDown) Res.drawable.street_side_unknown_l
                    else Res.drawable.street_side_unknown
                )
                parking?.painter(isUpsideDown, side == Side.RIGHT) ?: defaultPainter
            },
            onClickSide = { showPickerForSide = it },
            geometryRotation = geometryRotation,
            mapRotation = mapRotation,
            mapTilt = mapTilt,
            isLeftHandTraffic = isLeftHandTraffic,
            modifier = modifier,
            getFloatingPainter = { parking, side ->
                parking?.floatingIcon?.let { painterResource(it) }
            },
            lastPicked = lastPicked,
            lastPickedContentPadding = PaddingValues(start = 48.dp, end = 56.dp),
        )

        if (width != null) {
            val widthFormatted = if (width.toFloatOrNull() != null) width + "m" else width
            TextWithHalo(
                text = stringResource(Res.string.street_parking_street_width, widthFormatted),
                modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)
            )
        }
    }

    showPickerForSide?.let { side ->
        StreetParkingSelectionDialog(
            isUpsideDown = isUpsideDown(side),
            onDismissRequest = { showPickerForSide = null },
            onSelect = { parking ->
                onValueChanged(when (side) {
                    Side.LEFT -> value.copy(left = parking)
                    Side.RIGHT -> value.copy(right = parking)
                })
            },
        )
    }
}
