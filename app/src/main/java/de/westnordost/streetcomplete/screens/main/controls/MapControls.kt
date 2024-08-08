package de.westnordost.streetcomplete.screens.main.controls

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.messages.MessageDialog
import de.westnordost.streetcomplete.screens.main.overlays.OverlaySelectionDropdownMenu
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.LargeCreateIcon
import de.westnordost.streetcomplete.ui.common.StopRecordingIcon
import de.westnordost.streetcomplete.ui.common.UndoIcon
import de.westnordost.streetcomplete.ui.common.ZoomInIcon
import de.westnordost.streetcomplete.ui.common.ZoomOutIcon
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.launch
import kotlin.math.abs

// NOTE: this will eventually grow into something that should be renamed to MainScreen, i.e.
//       replacing MainFragment completely. For now, it is mostly only the controls and dialogs
//       and dropdowns triggered by that. But since this is big, we should take care to put any
//       elements that can meaningfully be put into an own composable in another file

/** Map controls shown on top of the map. */
@Composable
fun MapControls(
    viewModel: MainViewModel,
    hasEdits: Boolean,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickCompass: () -> Unit,
    onClickLocation: () -> Unit,
    onClickCreate: () -> Unit,
    onClickStopTrackRecording: () -> Unit,
    onClickUndo: () -> Unit,
    onClickDownload: () -> Unit,
    onClickUpload: () -> Unit,
    onExplainedNeedForLocationPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    val starsCount by viewModel.starsCount.collectAsState()
    val isShowingStarsCurrentWeek by viewModel.isShowingStarsCurrentWeek.collectAsState()

    val selectedOverlay by viewModel.selectedOverlay.collectAsState()

    val isAutoSync by viewModel.isAutoSync.collectAsState()
    val unsyncedEditsCount by viewModel.unsyncedEditsCount.collectAsState()

    val isTeamMode by viewModel.isTeamMode.collectAsState()
    val indexInTeam by viewModel.indexInTeam.collectAsState()

    val messagesCount by viewModel.messagesCount.collectAsState()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isUploadingOrDownloading by viewModel.isUploadingOrDownloading.collectAsState()

    val locationState by viewModel.locationState.collectAsState()
    val isNavigationMode by viewModel.isNavigationMode.collectAsState()
    val isFollowingPosition by viewModel.isFollowingPosition.collectAsState()
    val isRecordingTracks by viewModel.isRecordingTracks.collectAsState()

    val mapZoom by viewModel.mapZoom.collectAsState()
    val mapRotation by viewModel.mapRotation.collectAsState()
    val mapTilt by viewModel.mapTilt.collectAsState()

    val isCreateButtonEnabled by remember { derivedStateOf { mapZoom >= 18f } }

    var showOverlaysDropdown by remember { mutableStateOf(false) }
    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }

    fun onClickOverlays() {
        if (viewModel.hasShownOverlaysTutorial) {
            showOverlaysDropdown = true
        } else {
            showOverlaysTutorial = true
        }
    }

    fun onClickMessages() {
        coroutineScope.launch {
            shownMessage = viewModel.popMessage()
        }
    }

    LaunchedEffect(viewModel.hasShownTutorial) {
        if (!viewModel.hasShownTutorial && !isLoggedIn) {
            showIntroTutorial = true
        }
    }

    LaunchedEffect(isTeamMode) {
        // always show this toast on start to remind user that it is still on
        if (isTeamMode) {
            context.toast(R.string.team_mode_active)
        }
        // show this only once when turning it off
        else if (viewModel.teamModeChanged) {
            context.toast(R.string.team_mode_deactivated)
            viewModel.teamModeChanged = false
        }
    }

    // the layout of the buttons is not mirrored for different text directions
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        if (selectedOverlay?.isCreateNodeEnabled == true) {
            Crosshair()
        }
        Box(
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(bottom = 22.dp)
        ) {
            // stars counter
            StarsCounter(
                count = starsCount,
                modifier = Modifier
                    .defaultMinSize(minWidth = 96.dp)
                    .align(Alignment.TopStart)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { viewModel.toggleShowingCurrentWeek() },
                isCurrentWeek = isShowingStarsCurrentWeek,
                showProgress = isUploadingOrDownloading
            )

            // top controls
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messagesCount > 0) {
                    MessagesButton(
                        onClick = ::onClickMessages,
                        messagesCount = messagesCount
                    )
                }
                if (!isAutoSync) {
                    UploadButton(
                        onClick = onClickUpload,
                        unsyncedEditsCount = unsyncedEditsCount,
                        enabled = !isUploadingOrDownloading
                    )
                }
                Box {
                    OverlaySelectionButton(
                        onClick = ::onClickOverlays,
                        overlay = selectedOverlay
                    )
                    OverlaySelectionDropdownMenu(
                        expanded = showOverlaysDropdown,
                        onDismissRequest = { showOverlaysDropdown = false },
                        overlays = viewModel.overlays,
                        onSelect = { viewModel.selectOverlay(it) }
                    )
                }

                MainMenuButton(
                    onClick = { showMainMenuDialog = true },
                    indexInTeam = if (isTeamMode) indexInTeam else null
                )
            }

            // bottom-right controls
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isCompassVisible = abs(mapRotation) >= 1f || abs(mapTilt) >= 1f
                AnimatedVisibility(
                    visible = isCompassVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CompassButton(
                        onClick = onClickCompass,
                        modifier = Modifier.graphicsLayer(
                            rotationZ = mapRotation,
                            rotationX = mapTilt
                        )
                    )
                }
                MapButton(onClick = onClickZoomIn) { ZoomInIcon() }
                MapButton(onClick = onClickZoomOut) { ZoomOutIcon() }
                LocationStateButton(
                    onClick = onClickLocation,
                    state = locationState,
                    isNavigationMode = isNavigationMode,
                    isFollowing = isFollowingPosition,
                )
            }

            if (selectedOverlay?.isCreateNodeEnabled == true) {
                MapButton(
                    onClick = onClickCreate,
                    modifier = Modifier.align(BiasAlignment(0.333f, 1f)).padding(4.dp),
                    enabled = isCreateButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.secondaryVariant,
                    ),
                ) {
                    LargeCreateIcon()
                }
            }

            // bottom-left controls
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(4.dp),
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
                        // Don't allow undoing while uploading. Should prevent race conditions.
                        // (Undoing quest while also uploading it at the same time)
                        enabled = !isUploadingOrDownloading,
                    ) {
                        UndoIcon()
                    }
                }
            }
        }
    }

    shownMessage?.let { message ->
        MessageDialog(
            message = message,
            onDismissRequest = { shownMessage = null }
        )
    }

    if (showMainMenuDialog) {
        MainMenuDialog(
            onDismissRequest = { showMainMenuDialog = false },
            onClickProfile = { context.startActivity(Intent(context, UserActivity::class.java)) },
            onClickSettings = { context.startActivity(Intent(context, SettingsActivity::class.java)) },
            onClickAbout = { context.startActivity(Intent(context, AboutActivity::class.java)) },
            onClickDownload = onClickDownload,
            onClickEnterTeamMode = { showTeamModeWizard = true },
            onClickExitTeamMode = { viewModel.disableTeamMode() },
            isLoggedIn = isLoggedIn,
            indexInTeam = if (isTeamMode) indexInTeam else null,
        )
    }

    AnimatedScreenVisibility(showTeamModeWizard) {
        TeamModeWizard(
            onDismissRequest = { showTeamModeWizard = false },
            onFinished = { teamSize, indexInTeam ->
                viewModel.enableTeamMode(
                    teamSize = teamSize,
                    indexInTeam = indexInTeam
                )
            }
        )
    }

    AnimatedScreenVisibility(showOverlaysTutorial,) {
        OverlaysTutorialScreen(
            onDismissRequest = { showOverlaysTutorial = false },
            onFinished = { viewModel.hasShownOverlaysTutorial = true }
        )
    }

    AnimatedScreenVisibility(showIntroTutorial,) {
        IntroTutorialScreen(
            onDismissRequest = { showIntroTutorial = false },
            onExplainedNeedForLocationPermission = onExplainedNeedForLocationPermission,
            onFinished = { viewModel.hasShownTutorial = true },
        )
    }
}
