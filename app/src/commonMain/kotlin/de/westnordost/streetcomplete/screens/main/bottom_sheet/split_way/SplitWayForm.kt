package de.westnordost.streetcomplete.screens.main.bottom_sheet.split_way

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.scissorsPainter
import de.westnordost.streetcomplete.ui.common.FloatingOkButton
import de.westnordost.streetcomplete.ui.common.UndoIcon
import de.westnordost.streetcomplete.ui.common.bottom_sheet.BottomSheetFormScaffold
import de.westnordost.streetcomplete.ui.common.dialogs.AreYouSureDialog
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmDiscardDialog
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.distanceTo
import de.westnordost.streetcomplete.util.math.getSplitAt
import de.westnordost.streetcomplete.util.sound.SoundEffectPlayer
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/** Form that lets the user split an OSM way
 *
 *  [onCrosshairPositioned] reports the offset relative to the window of the crosshair - where to
 *  create a split - while this composable then expects to get the [crosshairPosition], i.e. where
 *  the crosshair is on the map.
 *
 *  [onScissorsPlaced] reports the position of the scissors (which is placed on the way, near the
 *  crosshairs) and [onSplitPositions] reports the positions of the cuts made so far.
 * */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
fun SplitWayForm(
    onConfirmed: (List<SplitPolylineAtPosition>) -> Unit,
    onDismiss: () -> Unit,
    crosshairPosition: LatLon?,
    onCrosshairPositioned: (offsetInWindow: Offset) -> Unit,
    onScissorsPlaced: (LatLon?) -> Unit,
    onSplitPositions: (List<LatLon>) -> Unit,
    way: Way,
    wayGeometry: ElementPolylinesGeometry,
    metersPerPixel: Double,
    soundEffectPlayer: SoundEffectPlayer = koinInject()
) {
    var confirmManySplits by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }

    var cuts by rememberSerializable { mutableStateOf(emptyList<SplitPolylineAtPosition>()) }

    val snipAnimation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val minDistanceToOtherCuts = metersPerPixel * 48.dp.toPx()
    val maxDistanceToCrosshair = metersPerPixel * 24.dp.toPx()
    val snapToVertexDistance = metersPerPixel * 12.dp.toPx()

    val scissorsPosition = crosshairPosition?.let {
        wayGeometry.polylines.first().getSplitAt(
            position = crosshairPosition,
            maxDistance = maxDistanceToCrosshair,
            snapToVertexDistance = snapToVertexDistance,
        )
    }

    val hasChanges = cuts.isNotEmpty()
    val isFormComplete = cuts.size >= if (way.isClosed) 2 else 1
    val canSplitHere = scissorsPosition != null
        && cuts.all { scissorsPosition.pos.distanceTo(it.pos) >= minDistanceToOtherCuts }

    LaunchedEffect(scissorsPosition?.pos) {
        onScissorsPlaced(scissorsPosition?.pos)
    }
    LaunchedEffect(cuts) {
        onSplitPositions(cuts.map { it.pos })
    }

    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        Icon(
            painter = painterResource(Res.drawable.crosshair),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(Dimensions.getOpenQuestFormMapPadding(LocalWindowInfo.current))
                .onGloballyPositioned { onCrosshairPositioned(it.positionInWindow()) },
            tint = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
        )

        BottomSheetFormScaffold(
            content = {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.quest_split_way_tutorial2),
                        style = MaterialTheme.typography.body2,
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
                    )

                    Box(Modifier.fillMaxWidth()) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = cuts.isNotEmpty(),
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    cuts = cuts.toMutableList().also { it.removeLastOrNull() }
                                    soundEffectPlayer.play("plop2.wav")
                                },
                                shape = CircleShape,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                UndoIcon()
                            }
                        }

                        OutlinedButton(
                            modifier = Modifier.align(Alignment.Center),
                            onClick = {
                                if (scissorsPosition == null) return@OutlinedButton
                                cuts = cuts.toMutableList().also { it.add(scissorsPosition) }

                                scope.launch {
                                    snipAnimation.animateTo(1f)
                                    snipAnimation.animateTo(0f)
                                }
                                soundEffectPlayer.play("snip.wav")
                            },
                            enabled = canSplitHere
                        ) {
                            Icon(
                                painter = scissorsPainter(snipAnimation.value),
                                contentDescription = stringResource(Res.string.split_way),
                                modifier = Modifier.rotate(-30f)
                            )
                        }
                    }
                }

                Divider()

                // button panel
                Column(Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                ) {
                    TextButton(onDismiss) { Text(stringResource(Res.string.cancel)) }
                }
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
            onDismissRequest = { confirmDiscard = true },
            onConfirmed = { onDismiss() },
        )
    }
}
