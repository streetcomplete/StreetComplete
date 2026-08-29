package de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.scissorsPainter
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalGetOffsetCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMarkersCallback
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.getSplitAt
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form that lets the user split an OSM way */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun SplitWayForm(
    onConfirmed: (splits: List<SplitPolylineAtPosition>) -> Unit,
    onDismiss: () -> Unit,
    mapPosition: LatLon?,
    way: Way,
    wayGeometry: ElementPolylinesGeometry,
    modifier: Modifier = Modifier,
) {
    var confirmManySplits by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }

    var cuts by rememberSerializable { mutableStateOf(emptyList<SplitPolylineAtPosition>()) }

    val metersPerDp = LocalMapMetersPerDp.current
    val minDistanceToOtherCuts = (metersPerDp * 24).dp.toPx().toDouble()
    val maxDistanceToCrosshair = (metersPerDp * 24).dp.toPx().toDouble()
    val snapToVertexDistance = (metersPerDp * 12).dp.toPx().toDouble()

    val mapMarkersCallback = LocalMapMarkersCallback.current

    val scissorsPosition = remember(mapPosition) {
        mapPosition?.let {
            wayGeometry.polylines.first().getSplitAt(
                position = mapPosition,
                maxDistance = maxDistanceToCrosshair,
                snapToVertexDistance = snapToVertexDistance,
            )
        }
    }

    val hasChanges = cuts.isNotEmpty()
    val isFormComplete = cuts.size >= if (way.isClosed) 2 else 1
    val canSplitHere = scissorsPosition != null
        && cuts.all { scissorsPosition.pos.distanceTo(it.pos) >= minDistanceToOtherCuts }

    val snipAnimation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cuts) {
        mapMarkersCallback?.invoke(
            cuts.map { Marker(ElementPointGeometry(it.pos), Res.drawable.scissors_cut) }
        )
    }

    BackHandler {
        if (hasChanges) {
            confirmDiscard = true
        } else {
            onDismiss()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (scissorsPosition != null) {
            val offset = LocalGetOffsetCallback.current?.invoke(scissorsPosition.pos)
            if (offset != null) {
                Image(
                    painter = scissorsPainter(snipAnimation.value),
                    contentDescription = null,
                    modifier = Modifier
                        .align(AbsoluteAlignment.TopLeft)
                        .size(72.dp)
                        .absoluteOffset(
                            x = offset.x.pxToDp() - 36.dp,
                            y = offset.y.pxToDp() - 36.dp
                        )
                        .rotate(-30f)
                )
            }
        }

        Icon(
            painter = painterResource(Res.drawable.crosshair),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Dimensions.getOpenQuestFormMapPadding(LocalWindowInfo.current)),
            tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
        )

        BottomSheetFormScaffold(
            content = {
                SplitWayFormContent(
                    onClickCancel = onDismiss,
                    canCutHere = canSplitHere,
                    onCut = {
                        if (scissorsPosition != null) {
                            scope.launch {
                                snipAnimation.animateTo(1f)
                                snipAnimation.animateTo(0f)
                            }

                            cuts = cuts.toMutableList().also { it.add(scissorsPosition) }
                        }
                    },
                    hasCuts = cuts.isNotEmpty(),
                    onUndo = { cuts = cuts.toMutableList().also { it.removeLastOrNull() } },
                )
            },
            fab = {
                FloatingOkButton(
                    visible = isFormComplete,
                    onClick = {
                        if (cuts.size > 2) {
                            confirmManySplits = true
                        } else {
                            onConfirmed(cuts)
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                )
            }
        )
    }

    if (confirmManySplits) {
        AreYouSureDialog(
            onDismissRequest = { confirmManySplits = false },
            onConfirmed = { onConfirmed(cuts) },
            text = { Text(stringResource(Res.string.quest_split_way_many_splits_confirmation_description)) }
        )
    }

    if (confirmDiscard) {
        ConfirmDiscardDialog(
            onDismissRequest = { confirmDiscard = false },
            onConfirmed = { onDismiss() },
        )
    }
}
