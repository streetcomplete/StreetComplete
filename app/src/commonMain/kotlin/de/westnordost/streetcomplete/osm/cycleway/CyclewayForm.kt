package de.westnordost.streetcomplete.osm.cycleway

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.oneway.Direction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.Side
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideForm
import de.westnordost.streetcomplete.util.ktx.noEntrySignDrawable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to input the cycleway situation */
@Composable
fun CyclewayForm(
    value: Sides<CyclewayAndDirection>,
    onValueChanged: (Sides<CyclewayAndDirection>) -> Unit,
    selectionMode: CyclewayFormSelectionMode,
    geometryRotation: Float,
    mapRotation: Float,
    mapTilt: Float,
    countryInfo: CountryInfo,
    roadDirection: Direction,
    modifier: Modifier = Modifier,
    lastPicked: List<Sides<CyclewayAndDirection>> = emptyList(),
    lastPickedContentPadding: PaddingValues = PaddingValues.Zero,
    enabled: Boolean = true,
    isLeftSideVisible: Boolean = true,
    isRightSideVisible: Boolean = true,
) {
    var showPickerForSide by remember { mutableStateOf<Side?>(null) }

    Box(
        modifier = modifier,
    ) {
        StreetSideForm(
            value = value,
            onValueChanged = onValueChanged,
            getIllustrationPainter = { cyclewayAndDirection, side ->
                cyclewayAndDirection
                    ?.getIcon(side == Side.RIGHT, countryInfo, roadDirection)
                    ?.let { painterResource(it) }
            },
            onClickSide = { side ->
                when (selectionMode) {
                    CyclewayFormSelectionMode.SELECT -> {
                        showPickerForSide = side
                    }

                    CyclewayFormSelectionMode.REVERSE -> {
                        onValueChanged(value.reverseDirection(side))
                    }
                }
            },
            geometryRotation = geometryRotation,
            mapRotation = mapRotation,
            mapTilt = mapTilt,
            isLeftHandTraffic = countryInfo.isLeftHandTraffic,
            getFloatingPainter = { cyclewayAndDirection, side ->
                cyclewayAndDirection
                    ?.getFloatingIcon(roadDirection, countryInfo.noEntrySignDrawable)
                    ?.let { painterResource(it) }
            },
            lastPicked = lastPicked,
            lastPickedContentPadding = lastPickedContentPadding,
            enabled = enabled,
            isLeftSideVisible = isLeftSideVisible,
            isRightSideVisible = isRightSideVisible,
        )
        if (selectionMode == CyclewayFormSelectionMode.REVERSE) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(4.dp),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = stringResource(Res.string.cycleway_reverse_direction_toast),
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    showPickerForSide?.let { side ->
        val current = value.get(side)
        val isRight = side == Side.RIGHT
        val direction = current?.direction ?: Direction.getDefault(isRight, countryInfo.isLeftHandTraffic)
        val selectableCycleways = getSelectableCycleways(countryInfo, isRight, countryInfo.isLeftHandTraffic, direction, roadDirection)

        SimpleItemSelectDialog(
            onDismissRequest = { showPickerForSide = null },
            columns = SimpleGridCells.Fixed(3),
            items = selectableCycleways,
            onSelected = { cycleway ->
                onValueChanged(
                    when (side) {
                        Side.LEFT -> value.copy(left = cycleway)
                        Side.RIGHT -> value.copy(right = cycleway)
                    }
                )
            },
            itemContent = { cycleway ->
                val icon = cycleway.getDialogIcon(isRight, countryInfo, roadDirection)
                val title = cycleway.getTitle(roadDirection)
                if (icon != null && title != null) {
                    ImageWithLabel(
                        painter = painterResource(icon),
                        label = stringResource(title),
                        imageRotation = if (countryInfo.isLeftHandTraffic) 180f else 0f
                    )
                }
            },
        )
    }
}

enum class CyclewayFormSelectionMode {
    /** Clicking on a side lets you select what type of cycleway there is */
    SELECT,
    /** Clicking on a side reverses the direction of the cycleway on that side */
    REVERSE
}

private fun Sides<CyclewayAndDirection>.reverseDirection(side: Side): Sides<CyclewayAndDirection> {
    val value = get(side)
    val newValue = value?.copy(direction = value.direction.reverse())
    return when (side) {
        Side.LEFT -> copy(left = newValue)
        Side.RIGHT -> copy(right = newValue)
    }
}

private fun Sides<CyclewayAndDirection>.get(side: Side): CyclewayAndDirection? =
    when (side) {
        Side.LEFT -> left
        Side.RIGHT -> right
    }
