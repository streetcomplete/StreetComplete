package de.westnordost.streetcomplete.osm.sidewalk

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.Side
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideForm
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to input the sidewalk situation */
@Composable
fun SidewalkForm(
    value: Sides<Sidewalk>,
    onValueChanged: (Sides<Sidewalk>) -> Unit,
    geometryRotation: Float,
    mapRotation: Float,
    mapTilt: Float,
    isLeftHandTraffic: Boolean,
    modifier: Modifier = Modifier,
    lastPicked: List<Sides<Sidewalk>> = emptyList(),
    lastPickedContentPadding: PaddingValues = PaddingValues.Zero,
) {
    var showPickerForSide by remember { mutableStateOf<Side?>(null) }

    StreetSideForm(
        value = value,
        onValueChanged = onValueChanged,
        getIllustrationPainter = { sidewalk, side ->
            sidewalk?.image?.let { painterResource(it) }
        },
        onClickSide = { showPickerForSide = it },
        geometryRotation = geometryRotation,
        mapRotation = mapRotation,
        mapTilt = mapTilt,
        isLeftHandTraffic = isLeftHandTraffic,
        modifier = modifier,
        getFloatingPainter = { sidewalk, side ->
            sidewalk?.floatingIcon?.let { painterResource(it) }
        },
        lastPicked = lastPicked,
        lastPickedContentPadding = lastPickedContentPadding,
    )

    showPickerForSide?.let { side ->
        val selectableItems = remember { listOf(Sidewalk.YES, Sidewalk.NO, Sidewalk.SEPARATE) }
        SimpleItemSelectDialog(
            onDismissRequest = { showPickerForSide = null },
            columns = SimpleGridCells.Fixed(3),
            items = selectableItems,
            onSelected = { sidewalk ->
                onValueChanged(
                    when (side) {
                        Side.LEFT -> value.copy(left = sidewalk)
                        Side.RIGHT -> value.copy(right = sidewalk)
                    }
                )
            },
            itemContent = { sidewalk ->
                val icon = sidewalk.icon
                val title = sidewalk.title
                if (icon != null && title != null) {
                    ImageWithLabel(painterResource(icon), stringResource(title))
                }
            }
        )
    }
}
