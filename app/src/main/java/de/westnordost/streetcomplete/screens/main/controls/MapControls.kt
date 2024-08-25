package de.westnordost.streetcomplete.screens.main.controls

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.MainViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistorySidebar
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.findClosestIntersection
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
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.ktx.pxToDp
import de.westnordost.streetcomplete.util.ktx.toast
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs

// NOTE: this will eventually grow into something that should be renamed to MainScreen, i.e.
//       replacing MainFragment completely. For now, it is mostly only the controls and dialogs
//       and dropdowns triggered by that. But since this is big, we should take care to put any
//       elements that can meaningfully be put into an own composable in another file

/** Map controls shown on top of the map. */
@Composable
fun MapControls(
    viewModel: MainViewModel,
    editHistoryViewModel: EditHistoryViewModel,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onClickCompass: () -> Unit,
    onClickLocation: () -> Unit,
    onClickLocationPointer: () -> Unit,
    onClickCreate: () -> Unit,
    onClickStopTrackRecording: () -> Unit,
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

    val mapCamera by viewModel.mapCamera.collectAsState()
    val displayedPosition by viewModel.displayedPosition.collectAsState()

    val editItems by editHistoryViewModel.editItems.collectAsState()
    val selectedEdit by editHistoryViewModel.selectedEdit.collectAsState()
    val hasEdits by remember { derivedStateOf { editItems.isNotEmpty() } }

    val isCreateButtonEnabled by remember { derivedStateOf { (mapCamera?.zoom ?: 0.0) >= 18.0 } }

    var showOverlaysDropdown by remember { mutableStateOf(false) }
    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }
    val showEditHistorySidebar by editHistoryViewModel.isShowingSidebar.collectAsState()

    val mapRotation = mapCamera?.rotation ?: 0.0
    val mapTilt = mapCamera?.tilt ?: 0.0

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

    Box {
        if (selectedOverlay?.isCreateNodeEnabled == true) {
            Crosshair()
        }

        val pointerPinRects = remember { mutableStateMapOf<String, Rect>() }
        val intersection = remember(displayedPosition, pointerPinRects) {
            findClosestIntersection(
                origin = pointerPinRects["frame"]?.center,
                target = displayedPosition,
                rects = pointerPinRects.values
            )
        }

        if (intersection != null) {
            val (offset, angle) = intersection
            val rotation = angle * 180 / PI - mapRotation
            PointerPinButton(
                onClick = onClickLocationPointer,
                rotate = rotation.toFloat(),
                modifier = Modifier.absoluteOffset(offset.x.pxToDp(), offset.y.pxToDp()),
            ) { Image(painterResource(R.drawable.location_dot_small), null) }
        }

        Box(
            modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .onGloballyPositioned { pointerPinRects["frame"] = it.boundsInRoot() }
                .padding(bottom = 22.dp)
        ) {
            // top-start controls
            Box(modifier = Modifier
                .align(Alignment.TopStart)
                .onGloballyPositioned { pointerPinRects["top-start"] = it.boundsInRoot() }
            ) {
                // stars counter
                StarsCounter(
                    count = starsCount,
                    modifier = Modifier
                        .defaultMinSize(minWidth = 96.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { viewModel.toggleShowingCurrentWeek() },
                    isCurrentWeek = isShowingStarsCurrentWeek,
                    showProgress = isUploadingOrDownloading
                )
            }

            // top-end controls
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .onGloballyPositioned { pointerPinRects["top-end"] = it.boundsInRoot() },
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

            // bottom-end controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .onGloballyPositioned { pointerPinRects["bottom-end"] = it.boundsInRoot() },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isCompassVisible = abs(mapRotation) >= 1.0 || abs(mapTilt) >= 1.0
                AnimatedVisibility(
                    visible = isCompassVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CompassButton(
                        onClick = onClickCompass,
                        modifier = Modifier.graphicsLayer(
                            rotationZ = -mapRotation.toFloat(),
                            rotationX = mapTilt.toFloat()
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
                    modifier = Modifier
                        .align(BiasAlignment(0.333f, 1f))
                        .padding(4.dp),
                    enabled = isCreateButtonEnabled,
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
                    .padding(4.dp)
                    .onGloballyPositioned { pointerPinRects["bottom-start"] = it.boundsInRoot() },
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
                        onClick = { editHistoryViewModel.showSidebar() },
                        // Don't allow undoing while uploading. Should prevent race conditions.
                        // (Undoing quest while also uploading it at the same time)
                        enabled = !isUploadingOrDownloading,
                    ) {
                        UndoIcon()
                    }
                }
            }
        }

        val dir = LocalLayoutDirection.current.dir
        AnimatedVisibility(
            visible = showEditHistorySidebar,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 * dir }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it / 2 * dir }),
        ) {
            EditHistorySidebar(
                editItems = editItems,
                selectedEdit = selectedEdit,
                onSelectEdit = { editHistoryViewModel.select(it.key) },
                onUndoEdit = { editHistoryViewModel.undo(it.key) },
                onDismissRequest = { editHistoryViewModel.hideSidebar() },
                featureDictionaryLazy = editHistoryViewModel.featureDictionaryLazy,
                getEditElement = editHistoryViewModel::getEditElement,
            )
        }
    }

    shownMessage?.let { message ->
        val questIcons = remember { viewModel.questTypes.map { it.icon } }
        MessageDialog(
            message = message,
            onDismissRequest = { shownMessage = null },
            allQuestIconIds = questIcons
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
        val questIcons = remember { viewModel.questTypes.map { it.icon } }
        TeamModeWizard(
            onDismissRequest = { showTeamModeWizard = false },
            onFinished = { teamSize, indexInTeam ->
                viewModel.enableTeamMode(
                    teamSize = teamSize,
                    indexInTeam = indexInTeam
                )
            },
            allQuestIconIds = questIcons
        )
    }

    AnimatedScreenVisibility(showOverlaysTutorial) {
        OverlaysTutorialScreen(
            onDismissRequest = { showOverlaysTutorial = false },
            onFinished = { viewModel.hasShownOverlaysTutorial = true }
        )
    }

    AnimatedScreenVisibility(showIntroTutorial) {
        IntroTutorialScreen(
            onDismissRequest = { showIntroTutorial = false },
            onExplainedNeedForLocationPermission = onExplainedNeedForLocationPermission,
            onFinished = { viewModel.hasShownTutorial = true },
        )
    }
}
