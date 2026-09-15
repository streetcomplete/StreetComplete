package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionDropdownMenu
import de.westnordost.streetcomplete.ui.common.LargeCreateIcon
import de.westnordost.streetcomplete.ui.common.StopRecordingIcon
import de.westnordost.streetcomplete.ui.common.UndoIcon

/** Map controls shown on top of the map, a.k.a. the map HUD */
@Composable
fun MainScreenControls(
    // clockwise:
    // stars counter
    starsCount: Int,
    isShowingStarsCurrentWeek: Boolean,
    isUploadingOrDownloading: Boolean,
    onToggleShowStarsCurrentWeek: () -> Unit,

    // messages button
    messagesCount: Int,
    onClickMessages: () -> Unit,

    // overlays button
    overlays: List<Overlay>,
    selectedOverlay: Overlay?,
    onSelectOverlay: (Overlay?) -> Unit,

    // main menu button
    shownUnsyncedEdits: Int,
    shownIndexInTeam: Int?,
    onClickMainMenu: () -> Unit,

    // zoom buttons
    showZoomButtons: Boolean,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onZoomDrag: (Float) -> Unit,

    // compass button
    mapRotation: Float,
    mapTilt: Float,
    onClickCompass: () -> Unit,

    // location button
    locationState: LocationState?,
    isNavigationMode: Boolean,
    isFollowingPosition: Boolean,
    onClickLocation: () -> Unit,

    // (stop) record track button
    isRecordingTracks: Boolean,
    onClickStopTrackRecording: () -> Unit,

    // create button
    isCreateNodeEnabled: Boolean,
    onClickCreate: () -> Unit,

    // undo button
    hasEdits: Boolean,
    isUndoEnabled: Boolean,
    onClickUndo: () -> Unit,

    // scale bar
    metersPerDp: Double,

    // attribution button
    attributions: List<String>,
    userHasMovedMap: Boolean,

    modifier: Modifier = Modifier,
) {
    var showOverlaysDropdown by remember { mutableStateOf(false) }

    // color for HUD elements without a background (e.g. scalebar, attribution button)
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colors.onSurface) {
        Box(modifier
            .fillMaxSize()
        ) {
            if (isCreateNodeEnabled) {
                Crosshair()
            }

            Box(Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {

                // top-start controls
                Box(Modifier.align(Alignment.TopStart)) {
                    // stars counter
                    StarsCounter(
                        count = starsCount,
                        modifier = Modifier
                            .defaultMinSize(minWidth = 96.dp)
                            .clickable(null, null, onClick = onToggleShowStarsCurrentWeek),
                        isCurrentWeek = isShowingStarsCurrentWeek,
                        showProgress = isUploadingOrDownloading
                    )
                }

                // top-end controls
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimatedVisibility(messagesCount > 0) {
                        MessagesButton(
                            onClick = onClickMessages,
                            messagesCount = messagesCount
                        )
                    }
                    if (overlays.isNotEmpty()) {
                        Box {
                            OverlaySelectionButton(
                                onClick = { showOverlaysDropdown = true },
                                overlay = selectedOverlay
                            )
                            OverlaySelectionDropdownMenu(
                                expanded = showOverlaysDropdown,
                                onDismissRequest = { showOverlaysDropdown = false },
                                overlays = overlays,
                                onSelect = onSelectOverlay
                            )
                        }
                    }

                    MainMenuButton(
                        onClick = onClickMainMenu,
                        unsyncedEditsCount = shownUnsyncedEdits,
                        indexInTeam = shownIndexInTeam
                    )
                }

                // bottom controls
                Column(Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                ) {
                    Box(Modifier.fillMaxWidth()) {
                        // bottom-end controls
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End,
                        ) {
                            CompassButton(
                                onClick = onClickCompass,
                                rotation = -mapRotation,
                                tilt = mapTilt,
                            )
                            if (showZoomButtons) {
                                ZoomButtons(
                                    onZoomIn = onClickZoomIn,
                                    onZoomOut = onClickZoomOut,
                                    onZoomDrag = onZoomDrag
                                )
                            }
                            if (locationState != null) {
                                LocationStateButton(
                                    onClick = onClickLocation,
                                    state = locationState,
                                    isNavigationMode = isNavigationMode,
                                    isFollowing = isFollowingPosition,
                                )
                            }
                        }

                        if (isCreateNodeEnabled) {
                            MapButton(
                                onClick = onClickCreate,
                                modifier = Modifier
                                    .align(BiasAlignment(0.333f, 1f))
                                    .padding(4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = MaterialTheme.colors.secondaryVariant,
                                ),
                            ) {
                                LargeCreateIcon()
                            }
                        }

                        // bottom-start controls
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isRecordingTracks) {
                                MapButton(
                                    onClick = onClickStopTrackRecording,
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.secondaryVariant,
                                    ),
                                ) {
                                    StopRecordingIcon()
                                }
                            }

                            if (hasEdits) {
                                MapButton(
                                    onClick = onClickUndo,
                                    enabled = isUndoEnabled,
                                ) {
                                    UndoIcon()
                                }
                            }
                        }
                    }

                    Box(Modifier.fillMaxWidth().padding(4.dp)) {
                        AttributionButton(
                            userHasMovedMap = userHasMovedMap,
                            attributions = attributions,
                            modifier = Modifier.align(Alignment.TopStart),
                            popupElevation = 4.dp,
                            textLinkStyles = TextLinkStyles()
                        )
                        ScaleBar(
                            metersPerDp = metersPerDp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 12.dp),
                            alignment = Alignment.End,
                        )
                    }
                }
            }
        }
    }
}
