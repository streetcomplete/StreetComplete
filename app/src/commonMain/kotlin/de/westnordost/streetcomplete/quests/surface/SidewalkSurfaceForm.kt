package de.westnordost.streetcomplete.quests.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.icon
import de.westnordost.streetcomplete.osm.surface.title
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.sidewalk_illustration_yes
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.OverlayedImageWithLabel
import de.westnordost.streetcomplete.ui.common.street_side_select.Side
import de.westnordost.streetcomplete.ui.common.street_side_select.StreetSideForm
import de.westnordost.streetcomplete.ui.util.ClipCirclePainter
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to select surfaces for the left and right sidewalk */
@Composable fun SidewalkSurfaceForm(
    value: Sides<Surface>,
    onValueChanged: (Sides<Surface>) -> Unit,
    geometryRotation: Float,
    mapRotation: Float,
    mapTilt: Float,
    isLeftHandTraffic: Boolean,
    modifier: Modifier = Modifier,
    lastPicked: List<Sides<Surface>> = emptyList(),
    isLeftSideVisible: Boolean = true,
    isRightSideVisible: Boolean = true,
) {
    var showPickerForSide by remember { mutableStateOf<Side?>(null) }

    StreetSideForm(
        value = value,
        onValueChanged = onValueChanged,
        getItemIllustration = { surface, side ->
            painterResource(Res.drawable.sidewalk_illustration_yes)
        },
        onClickSide = { showPickerForSide = it },
        geometryRotation = geometryRotation,
        mapRotation = mapRotation,
        mapTilt = mapTilt,
        isLeftHandTraffic = isLeftHandTraffic,
        modifier = modifier,
        itemContent = { surface, side ->
            OverlayedImageWithLabel(
                image = surface?.icon?.let { ClipCirclePainter(painterResource(it)) },
                label = surface?.title?.let { stringResource(it) }
            )
        },
        lastPicked = lastPicked,
        isLeftSideVisible = isLeftSideVisible,
        isRightSideVisible = isRightSideVisible,
    )

    showPickerForSide?.let { side ->
        SimpleItemSelectDialog(
            onDismissRequest = { showPickerForSide = null },
            columns = SimpleGridCells.Fixed(2),
            items = Surface.selectableValuesForWays,
            onSelected = { surface ->
                onValueChanged(when (side) {
                    Side.LEFT -> value.copy(left = surface)
                    Side.RIGHT -> value.copy(right = surface)
                })
            },
            itemContent = { surface ->
                val icon = surface.icon
                if (icon != null) {
                    ImageWithLabel(painterResource(icon), stringResource(surface.title))
                }
            }
        )
    }
}
