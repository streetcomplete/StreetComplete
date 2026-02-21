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
import de.westnordost.streetcomplete.resources.floating_question
import de.westnordost.streetcomplete.resources.sidewalk_illustration_no
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.common.item_select.ImageWithLabel
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
    hasSidewalkLeft: Boolean = true,
    hasSidewalkRight: Boolean = true,
) {
    var showPickerForSide by remember { mutableStateOf<Side?>(null) }

    StreetSideForm(
        value = value,
        onValueChanged = onValueChanged,
        getIllustrationPainter = { surface, side ->
            val hasSidewalk =
                side == Side.LEFT && hasSidewalkLeft ||
                side == Side.RIGHT && hasSidewalkRight

            painterResource(
                if (hasSidewalk) Res.drawable.sidewalk_illustration_yes
                else Res.drawable.sidewalk_illustration_no
            )
        },
        onClickSide = { showPickerForSide = it },
        geometryRotation = geometryRotation,
        mapRotation = mapRotation,
        mapTilt = mapTilt,
        isLeftHandTraffic = isLeftHandTraffic,
        modifier = modifier,
        getFloatingPainter = { surface, side ->
            val hasSidewalk =
                side == Side.LEFT && hasSidewalkLeft ||
                side == Side.RIGHT && hasSidewalkRight

            if (hasSidewalk) {
                surface?.icon?.let { ClipCirclePainter(painterResource(it)) }
                    ?: painterResource(Res.drawable.floating_question)
            } else null
        },
        lastPicked = lastPicked,
        isLeftSideEnabled = hasSidewalkLeft,
        isRightSideEnabled = hasSidewalkRight,
    )

    showPickerForSide?.let { side ->
        SimpleItemSelectDialog(
            onDismissRequest = { showPickerForSide = null },
            columns = SimpleGridCells.Fixed(3),
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
