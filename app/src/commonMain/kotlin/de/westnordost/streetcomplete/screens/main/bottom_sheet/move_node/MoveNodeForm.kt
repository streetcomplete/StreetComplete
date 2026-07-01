package de.westnordost.streetcomplete.screens.main.bottom_sheet.move_node

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfos
import de.westnordost.streetcomplete.data.meta.LengthUnit
import de.westnordost.streetcomplete.data.meta.get
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.Pin
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.util.ktx.length
import de.westnordost.streetcomplete.util.ktx.translate
import de.westnordost.streetcomplete.util.math.distanceTo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

// Require a minimum distance because the map is not perfectly precise, it may be hard to tell
// whether something really is misplaced without good aerial imagery.
// Also, POIs are objects with a certain extent, so as long as the node is within this extent, it's
// fine, there is little value of putting the point at exactly the center point of the POI
const val MIN_MOVE_DISTANCE = 1.0
// Move node functionality is meant for fixing slightly misplaced elements. If something moved far
// away, it is reasonable to assume there are more substantial changes required, also to nearby
// elements. Additionally, the default radius for highlighted elements is 30 m, so moving outside
// should not be allowed.
const val MAX_MOVE_DISTANCE = 30.0

/** Form that lets the user move an OSM node.
 *
 *  [onPinPositioned] reports the offset relative to the window of the pin - where to move the
 *  node to - while this composable then expects to get the [pinPosition], i.e. where the pin is on
 *  the map and [nodeOffsetInWindow] - the offset of the [node] relative to the window. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MoveNodeForm(
    onMoved: (position: LatLon) -> Unit,
    onDismiss: () -> Unit,
    onPinPositioned: (offsetInWindow: Offset) -> Unit,
    pinPosition: LatLon,
    node: Node,
    nodeOffsetInWindow: Offset?,
    elementEditType: ElementEditType,
    modifier: Modifier = Modifier,
    countryBoundaries: CountryBoundaries = koinInject(),
    countryInfos: CountryInfos = koinInject(),
) {
    var layoutCoordinates by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val displayUnit = remember {
        val countryInfo = countryInfos.get(countryBoundaries, node.position)
        val isFeetAndInch = countryInfo.lengthUnits.firstOrNull() == LengthUnit.FOOT_AND_INCH
        if (isFeetAndInch) MeasureDisplayUnitFeetInch(4) else MeasureDisplayUnitMeter(10)
    }
    val arrowColor = MaterialTheme.colors.primary
    val arrowWidthPx = 6.dp.toPx()
    val arrowHeadSizePx = 14.dp.toPx()

    val distance = pinPosition.distanceTo(node.position)
    var pinOffset by remember { mutableStateOf<Offset?>(null) }
    val nodeOffset = remember(nodeOffsetInWindow, layoutCoordinates) {
        nodeOffsetInWindow?.let { layoutCoordinates?.windowToLocal(nodeOffsetInWindow) }
    }

    var confirmDiscard by remember { mutableStateOf(false) }

    BackHandler {
        if (pinPosition != node.position) {
            confirmDiscard = true
        } else {
            onDismiss()
        }
    }

    Box(modifier = Modifier
        .fillMaxWidth()
        .onGloballyPositioned { layoutCoordinates = it }
        .drawBehind {
            drawArrow(
                color = arrowColor,
                strokeWidth = arrowWidthPx,
                arrowHeadSize = arrowHeadSizePx,
                start =  nodeOffset ?: return@drawBehind,
                end = pinOffset ?: return@drawBehind
            )
        }
    ) {
        BottomSheetFormScaffold(
            content = {
                MoveNodeFormContent(
                    distance = distance,
                    displayUnit = displayUnit,
                    onClickCancel = onDismiss,
                )
            },
            fab = {
                FloatingOkButton(
                    visible = distance in MIN_MOVE_DISTANCE..MAX_MOVE_DISTANCE,
                    onClick = { onMoved(pinPosition) },
                    modifier = Modifier.padding(8.dp),
                )
            }
        )

        Pin(
            iconPainter = painterResource(elementEditType.icon),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Dimensions.getOpenQuestFormMapPadding(LocalWindowInfo.current))
                .onGloballyPositioned {
                    pinOffset = it.positionInParent()
                    onPinPositioned(it.positionInWindow())
                }
        )
    }

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = true },
            onConfirmed = { onDismiss() },
        )
    }
}


private fun DrawScope.drawArrow(
    color: Color,
    strokeWidth: Float,
    arrowHeadSize: Float,
    start: Offset,
    end: Offset,
) {
    val angle = atan2(end.y.toDouble() - start.y.toDouble(), end.x.toDouble() - start.x.toDouble())
    // we shorten the line a bit so that the head of the arrow ends on [end] regardless of stroke
    // width
    val dist = (end - start).length() - strokeWidth / sqrt(2f)
    val actualEnd = start.translate(dist, angle)

    // draw line
    drawLine(
        color = color,
        start = start,
        end = actualEnd,
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    // draw arrow
    drawLine(
        color = color,
        start = actualEnd,
        end = actualEnd.translate(arrowHeadSize, angle + PI * 3 / 4),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = actualEnd,
        end = actualEnd.translate(arrowHeadSize, angle - PI * 3 / 4),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
