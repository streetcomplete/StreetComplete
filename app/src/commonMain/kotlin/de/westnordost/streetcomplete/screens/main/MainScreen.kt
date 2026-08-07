package de.westnordost.streetcomplete.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntSize
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MainBottomSheet
import de.westnordost.streetcomplete.screens.main.controls.MainScreenControls
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistorySidebar
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.errors.LastCrashEffect
import de.westnordost.streetcomplete.screens.main.errors.LastDownloadErrorEffect
import de.westnordost.streetcomplete.screens.main.errors.LastUploadErrorEffect
import de.westnordost.streetcomplete.screens.main.messages.MessageDialog
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.main.urlconfig.ApplyUrlConfigEffect
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.util.ReplaceBottomSheetTransitionSpec
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** Map controls shown on top of the map. */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    editHistoryViewModel: EditHistoryViewModel,
    mainBottomSheetViewModel: MainBottomSheetViewModel,
    onClickZoomIn: () -> Unit,
    onClickZoomOut: () -> Unit,
    onZoomDrag: (Float) -> Unit,
    onClickCompass: () -> Unit,
    onClickLocation: () -> Unit,
    onClickLocationPointer: () -> Unit,
    onClickCreate: () -> Unit,
    onClickStopTrackRecording: () -> Unit,
    onClickDownload: () -> Unit,
    onClickSettings: () -> Unit,
    onClickQuestSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickProfile: () -> Unit,
    onClickLogin: () -> Unit,
    onExplainedNeedForLocationPermission: () -> Unit,
    onSetMapMarkers: (Iterable<Marker>) -> Unit,
    onSolvedQuest: (icon: DrawableResource, position: LatLon) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val starsCount by viewModel.starsCount.collectAsState()
    val isShowingStarsCurrentWeek by viewModel.isShowingStarsCurrentWeek.collectAsState()

    val overlays by viewModel.overlays.collectAsState()
    val selectedOverlay by viewModel.selectedOverlay.collectAsState()
    val isCreateNodeEnabled by remember { derivedStateOf { selectedOverlay?.isCreateNodeEnabled == true } }

    val isAutoSync by viewModel.isAutoSync.collectAsState()
    val unsyncedEditsCount by viewModel.unsyncedEditsCount.collectAsState()

    val isTeamMode by viewModel.isTeamMode.collectAsState()
    val indexInTeam by viewModel.indexInTeam.collectAsState()

    val messagesCount by viewModel.messagesCount.collectAsState()

    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val isUploadingOrDownloading by viewModel.isUploadingOrDownloading.collectAsState()

    val urlConfig by viewModel.urlConfig.collectAsState()
    val lastCrashReport by viewModel.lastCrashReport.collectAsState()
    val lastDownloadError by viewModel.lastDownloadError.collectAsState()
    val lastUploadError by viewModel.lastUploadError.collectAsState()

    val locationState by viewModel.locationState.collectAsState()
    val isNavigationMode by viewModel.isNavigationMode.collectAsState()
    val isFollowingPosition by viewModel.isFollowingPosition.collectAsState()
    val isRecordingTracks by viewModel.isRecordingTracks.collectAsState()
    val userHasMovedCamera by viewModel.userHasMovedCamera.collectAsState()

    val mapCamera by viewModel.mapCamera.collectAsState()
    val metersPerDp by viewModel.metersPerDp.collectAsState()
    val displayedPosition by viewModel.displayedPosition.collectAsState()

    val showZoomButtons by viewModel.showZoomButtons.collectAsState()

    val isRequestingLogin by viewModel.isRequestingLogin.collectAsState()

    val showEditHistorySidebar by editHistoryViewModel.isShowingSidebar.collectAsState()

    val editItems by editHistoryViewModel.editItems.collectAsState()
    val selectedEdit by editHistoryViewModel.selectedEdit.collectAsState()
    val hasEdits by remember { derivedStateOf { editItems.isNotEmpty() } }

    val shownBottomSheet by mainBottomSheetViewModel.shownBottomSheet.collectAsState()
    val geometryOffsetInWindow by mainBottomSheetViewModel.geometryOffsetInWindow.collectAsState()

    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }
    var showToast by remember { mutableStateOf<Toast?>(null) }

    fun onClickUpload() {
        if (viewModel.isConnected) {
            viewModel.upload()
        } else {
            showToast = Toast.Offline
        }
    }

    fun sendErrorReport(error: Exception) {
        if (!viewModel.isSendErrorReportAvailable()) {
            showToast = Toast.NoEmailClient
        } else {
            viewModel.sendErrorReport(error)
        }
    }

    fun sendErrorReport(report: String) {
        if (!viewModel.isSendErrorReportAvailable()) {
            showToast = Toast.NoEmailClient
        } else {
            viewModel.sendErrorReport(report)
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
            showToast = Toast.TeamModeActive
        }
        // show this only once when turning it off
        else if (viewModel.teamModeChanged) {
            showToast = Toast.TeamModeDeactivated
            viewModel.teamModeChanged = false
        }
    }

    Box(modifier) {
        // TODO: Alternative to this would be to put the tutorial screens into a separate
        // navigation destination in a TBD MainNavHost after complete migration to Compose
        // (see #6255)
        if (!showIntroTutorial) {
            MainScreenControls(
                starsCount = starsCount,
                isShowingStarsCurrentWeek = isShowingStarsCurrentWeek,
                isUploadingOrDownloading = isUploadingOrDownloading,
                onToggleShowStarsCurrentWeek = { viewModel.toggleShowingCurrentWeek() },

                messagesCount = messagesCount,
                onClickMessages = { scope.launch { shownMessage = viewModel.popMessage() } },

                overlays = overlays,
                selectedOverlay = selectedOverlay,
                onSelectOverlay = { overlay ->
                    viewModel.selectOverlay(overlay)
                    if (!viewModel.hasShownOverlaysTutorial) {
                        showOverlaysTutorial = true
                    }
                },

                shownUnsyncedEdits = if (!isAutoSync) unsyncedEditsCount else 0,
                shownIndexInTeam = if (isTeamMode) indexInTeam else null,
                onClickMainMenu = { showMainMenuDialog = true },

                showZoomButtons = showZoomButtons,
                onClickZoomIn = onClickZoomIn,
                onClickZoomOut = onClickZoomOut,
                onZoomDrag = onZoomDrag,

                mapRotation = mapCamera?.rotation?.toFloat() ?: 0f,
                mapTilt = mapCamera?.tilt?.toFloat() ?: 0f,
                onClickCompass = onClickCompass,

                locationState = locationState,
                isNavigationMode = isNavigationMode,
                isFollowingPosition = isFollowingPosition,
                displayedLocationOffset = displayedPosition,
                onClickLocation = onClickLocation,
                onClickLocationPointer = onClickLocationPointer,

                isRecordingTracks = isRecordingTracks,
                onClickStopTrackRecording = onClickStopTrackRecording,

                isCreateNodeEnabled = isCreateNodeEnabled,
                onClickCreate = {
                    if ((mapCamera?.zoom ?: 0.0) >= 17.0) {
                        onClickCreate()
                    } else {
                        showToast = Toast.DownloadAreaTooBig
                    }
                },

                hasEdits = hasEdits,
                isUndoEnabled = !isUploadingOrDownloading,
                onClickUndo = { editHistoryViewModel.showSidebar() },

                metersPerDp = metersPerDp,
                userHasMovedMap = userHasMovedCamera,
            )
        }

        val dir = LocalLayoutDirection.current.dir
        AnimatedVisibility(
            visible = showEditHistorySidebar,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it * dir }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it * dir }),
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

        mapCamera?.let { mapCamera ->
            AnimatedContent(
                targetState = shownBottomSheet,
                transitionSpec = {
                    if (initialState != null && targetState != null) {
                        fadeIn() + slideInVertically { it / 16 } togetherWith
                        fadeOut() + slideOutVertically { it / 24 }
                    } else {
                        // Size transform with snap is necessary so that it doesn't animate the bounds
                        // from zero (=no form) which looks weird
                        (fadeIn() + slideInVertically { it } togetherWith
                        fadeOut() + slideOutVertically { it / 2 }) using SizeTransform(clip = false)
                    }
                },
            ) { shownBottomSheet ->
                if (shownBottomSheet != null) {
                    MainBottomSheet(
                        onDismiss = { mainBottomSheetViewModel.closeBottomSheet() },
                        onSolved = onSolvedQuest,
                        viewModel = mainBottomSheetViewModel,
                        shownBottomSheet = shownBottomSheet,
                        geometryOffsetInWindow = geometryOffsetInWindow,
                        mapRotation = mapCamera.rotation.toFloat(),
                        mapTilt = mapCamera.tilt.toFloat(),
                        mapPosition = mapCamera.position,
                        mapMetersPerDp = metersPerDp,
                        onSetMapMarkers = onSetMapMarkers
                    )
                }
            }
        }
    }

    shownMessage?.let { message ->
        val questIcons = remember { viewModel.allQuestTypes.map { it.icon } }
        MessageDialog(
            message = message,
            onDismissRequest = { shownMessage = null },
            allQuestIcons = questIcons,
            onClickOpenQuestSettings = onClickQuestSettings,
            onToggleDontNotifyAgain = { messageType, dontNotifyAgain ->
                viewModel.toggleDisableMessageType(messageType, dontNotifyAgain)
            }
        )
    }

    if (showMainMenuDialog) {
        MainMenuDialog(
            onDismissRequest = { showMainMenuDialog = false },
            onClickProfile = onClickProfile,
            onClickSettings = onClickSettings,
            onClickAbout = onClickAbout,
            onClickDownload = onClickDownload,
            onClickUpload = ::onClickUpload,
            onClickEnterTeamMode = { showTeamModeWizard = true },
            onClickExitTeamMode = { viewModel.disableTeamMode() },
            isLoggedIn = isLoggedIn,
            indexInTeam = if (isTeamMode) indexInTeam else null,
            unsyncedEditsCount = if (!isAutoSync) unsyncedEditsCount else null,
            isUploadingOrDownloading = isUploadingOrDownloading,
        )
    }

    urlConfig?.let { config ->
        ApplyUrlConfigEffect(
            urlConfig = config.urlConfig,
            presetNameAlreadyExists = config.alreadyExists,
            onApplyUrlConfig = { viewModel.applyUrlConfig(it) }
        )
    }
    lastDownloadError?.let { error ->
        LastDownloadErrorEffect(lastError = error, onReportError = ::sendErrorReport)
    }
    lastUploadError?.let { error ->
        LastUploadErrorEffect(lastError = error, onReportError = ::sendErrorReport)
    }
    lastCrashReport?.let { report ->
        LastCrashEffect(lastReport = report, onReport = ::sendErrorReport)
    }

    if (isRequestingLogin) {
        RequestLoginDialog(
            onDismissRequest = { viewModel.finishRequestingLogin() },
            onConfirmed = onClickLogin
        )
    }

    showToast?.messageResource?.let { message ->
        ToastPopup(
            onDismissRequest = { showToast = null },
            text = stringResource(message)
        )
    }

    AnimatedScreenVisibility(showTeamModeWizard) {
        val questIcons = remember { viewModel.allQuestTypes.map { it.icon } }
        TeamModeWizard(
            onDismissRequest = { showTeamModeWizard = false },
            onFinished = { teamSize, indexInTeam ->
                viewModel.enableTeamMode(
                    teamSize = teamSize,
                    indexInTeam = indexInTeam
                )
            },
            allQuestIcons = questIcons
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

private enum class Toast {
    Offline,
    TeamModeActive,
    TeamModeDeactivated,
    DownloadAreaTooBig,
    NoEmailClient
}

private val Toast.messageResource: StringResource get() =  when (this) {
    Toast.Offline -> Res.string.offline
    Toast.TeamModeActive -> Res.string.team_mode_active
    Toast.TeamModeDeactivated -> Res.string.team_mode_deactivated
    Toast.DownloadAreaTooBig -> Res.string.download_area_too_big
    Toast.NoEmailClient -> Res.string.no_email_client
}
