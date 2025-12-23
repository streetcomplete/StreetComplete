package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import de.westnordost.streetcomplete.screens.main.controls.ktx.drawPathsWithHalo
import de.westnordost.streetcomplete.screens.main.controls.ktx.drawTextWithHalo
import kotlin.math.ceil
import kotlin.math.roundToInt

/** Which measures to show on the scale bar. */
public data class ScaleBarMeasures(
    val primary: ScaleBarMeasure,
    val secondary: ScaleBarMeasure? = null,
)

/**
 * A scale bar composable that shows the current scale of the map in feet, meters or feet and meters
 * when zoomed in to the map, changing to miles and kilometers, respectively, when zooming out.
 *
 * @param metersPerDp how many meters are displayed in one device independent pixel (dp), i.e. the
 *   scale. See
 *   [CameraState.metersPerDpAtTarget][dev.sargunv.maplibrecompose.compose.CameraState.metersPerDpAtTarget]
 * @param modifier the [Modifier] to be applied to this layout node
 * @param measures which measures to show on the scale bar. If `null`, measures will be selected
 *   based on the system settings or otherwise the user's locale.
 * @param haloColor halo for better visibility when displayed on top of the map
 * @param haloWidth scale bar and text halo width
 * @param color scale bar and text color.
 * @param barWidth scale bar width
 * @param textStyle the text style. The text size is the deciding factor how large the scale bar is
 *   is displayed.
 * @param alignment horizontal alignment of the scale bar and text
 */
@Composable
fun ScaleBar(
    metersPerDp: Double,
    modifier: Modifier = Modifier,
    measures: ScaleBarMeasures = defaultScaleBarMeasures(),
    haloColor: Color = MaterialTheme.colors.surface,
    haloWidth: Dp = 0.dp,
    color: Color = contentColorFor(haloColor),
    barWidth: Dp = 2.dp,
    textStyle: TextStyle = MaterialTheme.typography.caption,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    // when map is not fully initialized yet
    if (metersPerDp == 0.0) return

    val textMeasurer = rememberTextMeasurer()
    // longest possible text
    val maxTextSizePx =
        remember(textMeasurer, textStyle) { textMeasurer.measure("5000 km", textStyle).size }
    val maxTextSize = with(LocalDensity.current) { maxTextSizePx.toSize().toDpSize() }

    // padding of text to bar stroke
    val textHorizontalPadding = 4.dp
    val textVerticalPadding = 0.dp

    // multiplied by 2.5 because the next stop can be the x2.5 of a previous stop (e.g. 2km -> 5km),
    // so the bar can end at approx 1/2.5th of the total width. We want to avoid that the bar
    // intersects with the text, i.e. is drawn behind the text
    val totalMaxWidth = maxTextSize.width * 2.5f + (textHorizontalPadding + barWidth) * 2f

    val fullStrokeWidth = haloWidth * 2 + barWidth

    val textCount = if (measures.secondary != null) 2 else 1
    val totalHeight = (maxTextSize.height + textVerticalPadding) * textCount + fullStrokeWidth

    BoxWithConstraints(modifier.size(totalMaxWidth, totalHeight)) {
        // scale bar start/end should not overlap horizontally with canvas bounds
        val maxBarLength = maxWidth - fullStrokeWidth

        val params1 = scaleBarParameters(measures.primary, metersPerDp, maxBarLength)
        val params2 = measures.secondary?.let { scaleBarParameters(it, metersPerDp, maxBarLength) }

        Canvas(Modifier.fillMaxSize()) {
            val fullStrokeWidthPx = fullStrokeWidth.toPx()
            val textHeightPx = maxTextSizePx.height
            val textHorizontalPaddingPx = textHorizontalPadding.toPx()
            val textVerticalPaddingPx = textVerticalPadding.toPx()

            // bar ends should go to the vertical center of the text
            val barEndsHeightPx = textHeightPx / 2f + textVerticalPadding.toPx() + fullStrokeWidthPx / 2f

            var y = 0f
            val paths = ArrayList<List<Offset>>(2)
            val texts = ArrayList<Pair<Offset, TextLayoutResult>>(2)

            val alignmentSpacePx = ceil(size.width - fullStrokeWidthPx).toInt()

            if (true) { // just want a scope here
                val barLengthPx = params1.barLength.toPx().roundToInt()
                val offsetX =
                    alignment.align(
                        size = barLengthPx,
                        space = alignmentSpacePx,
                        layoutDirection = layoutDirection,
                    )
                paths.add(
                    listOf(
                        Offset(offsetX + fullStrokeWidthPx / 2f, 0f + textHeightPx / 2f),
                        Offset(0f, barEndsHeightPx),
                        Offset(barLengthPx.toFloat(), 0f),
                        Offset(0f, -barEndsHeightPx),
                    )
                )
                texts.add(
                    Pair(
                        Offset(textHorizontalPaddingPx + fullStrokeWidthPx, 0f),
                        textMeasurer.measure(params1.text, textStyle),
                    )
                )

                y += textHeightPx + textVerticalPaddingPx
            }

            if (params2 != null) {
                val barLengthPx = params2.barLength.toPx().roundToInt()
                val offsetX =
                    alignment.align(
                        size = barLengthPx,
                        space = alignmentSpacePx,
                        layoutDirection = layoutDirection,
                    )
                paths.add(
                    listOf(
                        Offset(offsetX + fullStrokeWidthPx / 2f, y + fullStrokeWidthPx / 2f + barEndsHeightPx),
                        Offset(0f, -barEndsHeightPx),
                        Offset(barLengthPx.toFloat(), 0f),
                        Offset(0f, +barEndsHeightPx),
                    )
                )
                texts.add(
                    Pair(
                        Offset(
                            textHorizontalPaddingPx + fullStrokeWidthPx,
                            y + textVerticalPaddingPx + fullStrokeWidthPx,
                        ),
                        textMeasurer.measure(params2.text, textStyle),
                    )
                )
            }

            drawPathsWithHalo(
                color = color,
                haloColor = haloColor,
                paths = paths,
                strokeWidth = barWidth.toPx(),
                haloWidth = haloWidth.toPx(),
                cap = StrokeCap.Round,
            )

            for ((offset, textLayoutResult) in texts) {
                val offsetX =
                    alignment.align(
                        size = textLayoutResult.size.width,
                        space = ceil(size.width - 2 * offset.x).toInt(),
                        layoutDirection = layoutDirection,
                    ) + offset.x
                drawTextWithHalo(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(offsetX, offset.y),
                    color = color,
                    haloColor = haloColor,
                    haloWidth = haloWidth.toPx(),
                )
            }
        }
    }
}

private data class ScaleBarParams(val barLength: Dp, val text: String)

@Composable
private fun scaleBarParameters(
    measure: ScaleBarMeasure,
    metersPerDp: Double,
    maxBarLength: Dp,
): ScaleBarParams {
    val max = maxBarLength.value * metersPerDp / measure.unitInMeters
    val stop = findStop(max, measure.stops)
    return ScaleBarParams((stop * measure.unitInMeters / metersPerDp).dp, measure.getText(stop))
}

/**
 * find the largest stop in the list of stops (sorted in ascending order) that is below or equal
 * [max].
 */
private fun findStop(max: Double, stops: List<Double>): Double {
    val i = stops.binarySearch { it.compareTo(max) }
    return if (i >= 0) stops[i] else stops[(-i - 2).coerceAtLeast(0)]
}
