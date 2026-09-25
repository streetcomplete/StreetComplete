package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.maplibre.compose.overlay.ScaleBar
import org.maplibre.compose.overlay.ScaleBarDefaults
import org.maplibre.compose.overlay.ScaleBarMeasures

/**
 * A scale bar composable that shows the current scale of the map in feet, meters or feet and meters
 * when zoomed in to the map, changing to miles and kilometers, respectively, when zooming out.
 *
 * @param metersPerDp how many meters are displayed in one device independent pixel (dp), i.e. the
 *   scale. See
 *   [Viewport.metersPerDpAtTarget][org.maplibre.compose.camera.Viewport.metersPerDpAtTarget]
 * @param modifier the [Modifier] to be applied to this layout node
 * @param measures which measures to show on the scale bar. The default follows the system settings,
 *   or otherwise the user's locale.
 * @param color scale bar and text color.
 * @param haloColor halo for better visibility when displayed on top of the map
 * @param haloWidth scale bar and text halo width
 * @param barWidth scale bar width
 * @param textStyle the text style. The text size is the deciding factor how large the scale bar is
 *   is displayed.
 * @param alignment horizontal alignment of the scale bar and text
 */
@Composable
fun ScaleBar(
    metersPerDp: Double,
    modifier: Modifier = Modifier,
    measures: ScaleBarMeasures = ScaleBarDefaults.measures(),
    haloColor: Color = MaterialTheme.colors.surface,
    color: Color = contentColorFor(haloColor),
    haloWidth: Dp = 0.dp,
    barWidth: Dp = ScaleBarDefaults.BarWidth,
    textStyle: TextStyle = MaterialTheme.typography.caption,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    ScaleBar(
        metersPerDp = metersPerDp,
        modifier = modifier,
        measures = measures,
        color = color,
        haloColor = haloColor,
        haloWidth = haloWidth,
        barWidth = barWidth,
        textStyle = textStyle,
        alignment = alignment
    )
}
