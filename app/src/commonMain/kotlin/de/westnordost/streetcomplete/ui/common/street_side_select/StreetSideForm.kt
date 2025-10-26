package de.westnordost.streetcomplete.ui.common.street_side_select

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.get
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.street_side_unknown
import de.westnordost.streetcomplete.resources.street_side_unknown_l
import de.westnordost.streetcomplete.ui.common.last_picked.LastPickedChipsRow
import org.jetbrains.compose.resources.painterResource

/** Form to input the something for the left and right side of a street */
@Composable  fun <T> StreetSideForm(
    value: Sides<T>,
    onValueChanged: (Sides<T>) -> Unit,
    getIllustrationPainter: @Composable (T?, Side) -> Painter?,
    onClickSide: (Side) -> Unit,
    geometryRotation: Float,
    mapRotation: Float,
    mapTilt: Float,
    isLeftHandTraffic: Boolean,
    modifier: Modifier = Modifier,
    getFloatingPainter: @Composable (T?, Side) -> Painter? = { _, _ -> null },
    lastPicked: List<Sides<T>> = emptyList(),
    enabled: Boolean = true,
    isLeftSideVisible: Boolean = true,
    isRightSideVisible: Boolean = true,
) {
    val rotation = geometryRotation - mapRotation

    val unknownPainter = painterResource(
        if (!isLeftHandTraffic) Res.drawable.street_side_unknown
        else Res.drawable.street_side_unknown_l
    )

    Box(modifier = modifier
        .fillMaxWidth()
        .height(160.dp)
    ) {
        StreetSideIllustration(
            value = value,
            getIllustrationPainter = { v, side ->
                getIllustrationPainter(v, side) ?: unknownPainter
            },
            onClickSide = onClickSide,
            rotation = rotation,
            modifier = Modifier.align(Alignment.Center),
            getFloatingPainter = getFloatingPainter,
            enabled = enabled,
            isLeftSideVisible = isLeftSideVisible,
        )

        MiniCompass(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            rotation = -mapRotation,
            tilt = mapTilt
        )

        if (enabled && lastPicked.isNotEmpty() && value.left == null && value.right == null) {
            LastPickedChipsRow(
                items = lastPicked,
                onClick = { onValueChanged(it) },
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomStart),
                chipBorder = BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                chipContentPadding = PaddingValues.Zero,
            ) { value ->
                StreetSideIllustration(
                    value = value,
                    getIllustrationPainter = { v, side ->
                        getIllustrationPainter(v, side) ?: unknownPainter
                    },
                    rotation = rotation,
                    modifier = Modifier.size(56.dp, 40.dp),
                    getFloatingPainter = getFloatingPainter,
                    isRightSideVisible = isRightSideVisible,
                )
            }
        }
    }
}
