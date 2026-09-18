package de.westnordost.streetcomplete.screens.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.BottomSheetFormState
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MainBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MainBottomSheetMapOverlay
import de.westnordost.streetcomplete.screens.main.bottom_sheet.rememberBottomSheetFormState
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.controls.MainScreenControls
import de.westnordost.streetcomplete.screens.main.controls.PointerPinButton
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistorySidebar
import de.westnordost.streetcomplete.screens.main.errors.LastCrashEffect
import de.westnordost.streetcomplete.screens.main.errors.LastDownloadErrorEffect
import de.westnordost.streetcomplete.screens.main.errors.LastUploadErrorEffect
import de.westnordost.streetcomplete.screens.main.map.CameraInspectionEffect
import de.westnordost.streetcomplete.screens.main.map.BASE_STYLE
import de.westnordost.streetcomplete.screens.main.map.MainMap
import de.westnordost.streetcomplete.screens.main.map.MainMapContent
import de.westnordost.streetcomplete.screens.main.map.PinsMode
import de.westnordost.streetcomplete.screens.main.map.crosshairPosition
import de.westnordost.streetcomplete.screens.main.map.getTrackBearing
import de.westnordost.streetcomplete.screens.main.map.offsetInWindow
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapCameraState
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapTrackState
import de.westnordost.streetcomplete.screens.main.map.toStreetCompleteBoundingBox
import de.westnordost.streetcomplete.screens.main.messages.MessageDialog
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.main.urlconfig.ApplyUrlConfigEffect
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.LocalMapMetersPerDp
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toLocation
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.ClickEvent
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.location.HeadingProvider
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationPermission
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.SystemSettingsLauncher
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.GeographicLayout
import org.maplibre.compose.style.BaseStyle

/** The map and its controls, forms, and sidebars. */
@Composable
fun MainScreen(
    onClickSettings: () -> Unit,
    onClickQuestSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickProfile: () -> Unit,
    onClickLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    runtime: MapRuntime = koinInject(),
    locationProvider: LocationProvider = koinInject(),
    headingProvider: HeadingProvider = koinInject(),
    systemSettingsLauncher: SystemSettingsLauncher = koinInject(),
) {
    //region state
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

    val showZoomButtons by viewModel.showZoomButtons.collectAsState()

    val isRequestingLogin by viewModel.isRequestingLogin.collectAsState()

    val emailAppLauncher = rememberEmailAppLauncher()
    val mapAppLauncher = rememberMapAppLauncher()

    var confirmReplaceDownload by remember { mutableStateOf(false) }
    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var showLocationPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showApplicationSettingsDialog by remember { mutableStateOf(false) }
    var showLocationSettingsDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }
    var showToast by remember { mutableStateOf<Toast?>(null) }

    var lastLongPress by remember { mutableStateOf<MapClick?>(null) }
    var showMapContextMenu by remember { mutableStateOf(false) }
    var lastQuestSolved by remember { mutableStateOf<QuestSolvedEvent?>(null) }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val windowInfo = LocalWindowInfo.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val sheet = rememberMainSheetState(viewModel.bottomSheet, viewModel.editHistory)
    val tracks = rememberMainMapTrackState()
    val location = rememberMainLocationState(locationProvider, headingProvider)
    val selection = sheet.selection
    val shownBottomSheet = sheet.shownBottomSheet
    // The form's state is read both by the bottom sheet and by what the form places on the map.
    val shownForm = shownBottomSheet?.takeUnless { it is ShownBottomSheet.EditHistory }?.let { shown ->
        key(sheet.id) { ShownForm(sheet.id, shown, rememberBottomSheetFormState(shown)) }
    }
    //endregion

    //region map
    val downloadedTiles by viewModel.map.downloadedTiles.collectAsStateWithLifecycle()
    val geoUri by viewModel.geoUri.collectAsState()
    var mapOrigin by remember { mutableStateOf(Offset.Zero) }
    val highlightedMarkers by produceState<List<Marker>>(emptyList(), shownBottomSheet) {
        value = shownBottomSheet?.let { viewModel.bottomSheet.getHighlightedMarkers(it) }.orEmpty()
    }
    val markers = sheet.formMarkers ?: highlightedMarkers
    // hidden behind quest forms, which highlight elements themselves, and behind the edit history
    val showOverlay = selectedOverlay != null &&
        selection !is MainBottomSheetSelection.Quest &&
        selection !is MainBottomSheetSelection.EditHistory

    val initialCamera = remember(viewModel) { viewModel.initialCamera }
    val pinsMode = when (selection) {
        is MainBottomSheetSelection.EditHistory -> PinsMode.EditHistory
        null, is MainBottomSheetSelection.CreateNote -> PinsMode.Quests
        else -> PinsMode.None
    }
    val mapState = rememberMapState(runtime, BaseStyle.Json(BASE_STYLE), initialCameraPosition = initialCamera) {
        MainMapContent(
            source = viewModel.map,
            location = location.location,
            heading = location.headingDegrees,
            isRecording = tracks.isRecording,
            trackpoints = tracks.recentTrackPositions,
            oldTrackpointsLists = tracks.olderTrackPositions,
            shownBottomSheet = shownBottomSheet,
            shownMarkers = markers,
            hiddenLabels = selectedOverlay?.hiddenLabels.orEmpty(),
            showOverlay = showOverlay,
            downloadedTiles = downloadedTiles,
            pinsMode = pinsMode,
            isSelectable = !sheet.isFormOpen,
            onClickQuest = { sheet.show(MainBottomSheetSelection.Quest(it)) },
            onClickEdit = { sheet.show(MainBottomSheetSelection.EditHistory(it)) },
            onClickElement = { key ->
                selectedOverlay?.let { sheet.show(MainBottomSheetSelection.Overlay(it.name, key)) }
            },
        )
    }
    val cameraState = rememberMainMapCameraState(mapState, viewModel.initiallyFollowing, viewModel.initiallyNavigating)
    val mapCamera = mapState.cameraPosition
    val viewport = mapState.viewport
    val metersPerDp = remember(viewport, mapCamera) {
        mapState.metersPerDpAtLatitude(mapCamera.target.latitude) ?: 0.0
    }
    val sheetPadding = Dimensions.getOpenQuestFormMapPadding(windowInfo)
    val cameraPadding = cameraState.padding(sheetPadding)
    /** The crosshair's position: what an open form refers to on the map */
    val mapPosition = mapState.crosshairPosition(sheetPadding, layoutDirection) ?: mapCamera.target.toLatLon()
    //endregion

    //region actions
    fun getOffset(position: LatLon): Offset? = mapState.offsetInWindow(position, mapOrigin, density)
    fun getCrosshairPosition(): LatLon? = mapState.crosshairPosition(sheetPadding, layoutDirection)
    fun ClickEvent.toMapClick(): MapClick? =
        position?.let { MapClick(it.toLatLon(), screenOffset, clickAreaSizeInMeters = metersPerDp * 14) }
    fun followLocation() {
        scope.launch { cameraState.locate(location.position, getTrackBearing(tracks.currentTrack)) }
    }
    fun zoomBy(amount: Double) {
        scope.launch { cameraState.zoomBy(amount) }
    }
    fun composeNote(position: LatLon, trackpoints: List<Trackpoint>? = null) {
        sheet.show(MainBottomSheetSelection.CreateNote(position, trackpoints))
    }
    fun download() {
        val displayedArea = mapState.viewport?.visibleBounds?.toStreetCompleteBoundingBox()
        if (displayedArea == null) {
            showToast = Toast.CannotFindBounds
        } else if (!viewModel.download(displayedArea, mapState.cameraPosition.target.toLatLon())) {
            showToast = Toast.DownloadAreaTooBig
        }
    }
    fun clickLocation() {
        val permission = locationProvider.permission.value
        if (permission is LocationPermission.NotGranted) {
            when {
                permission.canRequest == false -> {
                    if (systemSettingsLauncher.canOpenApplicationSettings) showApplicationSettingsDialog = true
                    else showToast = Toast.NoLocation
                }
                permission.shouldShowRationale -> showLocationPermissionRationaleDialog = true
                else -> locationProvider.requestPermission()
            }
        } else if (location.state == LocationState.ALLOWED) {
            if (systemSettingsLauncher.canOpenLocationServicesSettings) showLocationSettingsDialog = true
            else showToast = Toast.NoLocation
        } else if (!cameraState.isFollowingPosition) {
            followLocation()
        } else scope.launch {
            cameraState.setNavigationMode(!cameraState.isNavigationMode, location.position, getTrackBearing(tracks.currentTrack))
        }
    }

    fun onClickDownload() {
        if (viewModel.isConnected) {
            if (viewModel.isUserInitiatedDownloadInProgress) {
                confirmReplaceDownload = true
            } else {
                download()
            }
        } else {
            showToast = Toast.Offline
        }
    }

    fun onClickUpload() {
        if (viewModel.isConnected) {
            viewModel.upload()
        } else {
            showToast = Toast.Offline
        }
    }

    fun sendErrorReport(errorReport: String) {
        if (!emailAppLauncher.isAvailable()) {
            showToast = Toast.NoEmailClient
        } else {
            emailAppLauncher.compose(
                email = ApplicationConstants.ERROR_REPORTS_EMAIL,
                subject = ApplicationConstants.USER_AGENT + " " + "Error Report",
                body = "Describe how to reproduce it here:\n\n\n\n$errorReport"
            )
        }
    }

    fun sendErrorReport(error: Exception) {
        scope.launch {
            val report = viewModel.createErrorReport(error)
            sendErrorReport(report)
        }
    }
    //endregion

    //region effects
    LaunchedEffect(geoUri) {
        geoUri?.let {
            cameraState.moveTo(it)
            viewModel.consumeGeoUri()
        }
    }
    // Apply the MapLibre viewport to StreetComplete's quest and overlay data sources.
    LaunchedEffect(mapState, viewModel.map) {
        snapshotFlow { mapState.cameraPosition.zoom to mapState.viewport?.visibleBounds }
            .collect { (zoom, bounds) -> viewModel.map.onViewportChanged(zoom, bounds?.toStreetCompleteBoundingBox()) }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.saveCamera(mapState.cameraPosition, cameraState.isFollowingPosition, cameraState.isNavigationMode)
    }
    LaunchedEffect(location, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            location.collectUpdates { event ->
                when (event) {
                    is LocationEvent.Update -> {
                        val fix = event.toLocation()
                        // Survey checking receives every fix, including ones too inaccurate for a track.
                        viewModel.map.onLocationChanged(fix)
                        tracks.addLocation(event.measurement)
                        launch { cameraState.followLocation(fix.position, getTrackBearing(tracks.currentTrack)) }
                    }
                    is LocationEvent.Unavailable -> {
                        tracks.clear()
                        launch { cameraState.setNavigationMode(false, null, null) }
                    }
                }
            }
        }
    }
    CameraInspectionEffect(cameraState, sheet, location, tracks)
    LaunchedEffect(selectedOverlay) {
        val selection = sheet.selection as? MainBottomSheetSelection.Overlay
        if (selection != null && selection.name != selectedOverlay?.name) sheet.close()
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
    //endregion

    //region content
    Box(modifier) {
        MainMap(
            state = mapState,
            modifier = Modifier.fillMaxSize().onGloballyPositioned { mapOrigin = it.positionInWindow() },
            cameraPadding = cameraPadding,
            onPan = { cameraState.onPan(location.location != null) },
            onMapClick = { event ->
                when (sheet.selection) {
                    null -> {}
                    is MainBottomSheetSelection.EditHistory -> sheet.close()
                    // forms react to clicks near the click position, e.g. to suggest a name
                    else -> event.toMapClick()?.let { sheet.lastMapClick = it }
                }
                ClickResult.Consume
            },
            onMapLongClick = { event ->
                if (!sheet.isOpen) {
                    event.toMapClick()?.let {
                        lastLongPress = it
                        showMapContextMenu = true
                    }
                }
                ClickResult.Consume
            },
            overlay = {
                if (shownForm != null) {
                    CompositionLocalProvider(LocalMapMetersPerDp provides metersPerDp) {
                        MainBottomSheetMapOverlay(shownForm.sheet, shownForm.formState, mapPosition)
                    }
                }

                // the pointer stays within the map area not covered by system bars or a form
                GeographicLayout(
                    Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(if (sheet.isFormOpen) sheetPadding else PaddingValues(0.dp))
                ) {
                    if (!showIntroTutorial) {
                        location.location?.position?.let { position ->
                            PointerPinButton(targetPosition = position, onClick = ::followLocation) {
                                Image(painterResource(Res.drawable.location_dot_small), null)
                            }
                        }
                    }
                }

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
                        onClickZoomIn = { zoomBy(1.0) },
                        onClickZoomOut = { zoomBy(-1.0) },
                        onZoomDrag = { zoomBy(it / 20.0) },

                        mapRotation = mapCamera.bearing.toFloat(),
                        mapTilt = mapCamera.tilt.toFloat(),
                        onClickCompass = { scope.launch { cameraState.resetCompass() } },

                        locationState = location.state,
                        isNavigationMode = cameraState.isNavigationMode,
                        isFollowingPosition = cameraState.isFollowingPosition,
                        onClickLocation = ::clickLocation,

                        isRecordingTracks = tracks.isRecording,
                        onClickStopTrackRecording = {
                            val recorded = tracks.stopRecording()
                            location.position?.let { composeNote(it, recorded.takeIf { it.isNotEmpty() }) }
                        },

                        isCreateNodeEnabled = isCreateNodeEnabled,
                        onClickCreate = {
                            if (mapCamera.zoom >= 17.0) {
                                selectedOverlay?.let { overlay ->
                                    val position = getCrosshairPosition()
                                    sheet.show(MainBottomSheetSelection.Overlay(overlay.name))
                                    position?.let { cameraState.preserveCrosshairPosition(it) }
                                }
                            } else {
                                showToast = Toast.DownloadAreaTooBig
                            }
                        },

                        hasEdits = sheet.hasEdits,
                        isUndoEnabled = !isUploadingOrDownloading,
                        onClickUndo = sheet::showEditHistory,

                        metersPerDp = metersPerDp,
                    )
                }
            },
        )

        val dir = LocalLayoutDirection.current.dir
        AnimatedVisibility(
            visible = selection is MainBottomSheetSelection.EditHistory && sheet.hasEdits,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it * dir }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it * dir }),
        ) {
            EditHistorySidebar(
                editItems = sheet.editItems.orEmpty(),
                selectedEdit = (shownBottomSheet as? ShownBottomSheet.EditHistory)?.edit,
                onSelectEdit = { sheet.show(MainBottomSheetSelection.EditHistory(it.key)) },
                onUndoEdit = { viewModel.editHistory.undo(it.key) },
                onDismissRequest = sheet::close,
                getEditElement = viewModel.editHistory::getEditElement,
            )
        }

        AnimatedContent(
            targetState = shownForm,
            contentKey = { it?.id },
            transitionSpec = {
                if (initialState != null && targetState != null) {
                    fadeIn() + slideInVertically { it / 16 } togetherWith fadeOut()
                } else {
                    // Size transform with snap is necessary so that it doesn't animate the bounds
                    // from zero (=no form) which looks weird
                    (fadeIn() + slideInVertically { it } togetherWith
                    fadeOut() + slideOutVertically { it / 2 }) using SizeTransform(clip = false)
                }
            },
        ) { content ->
            if (content != null) {
                val (id, shownBottomSheet, formState) = content
                sheet.formStateHolder.SaveableStateProvider(id) {
                    MainBottomSheet(
                        onDismiss = sheet::close,
                        onSolved = { icon, position ->
                            getOffset(position)?.let { lastQuestSolved = QuestSolvedEvent(icon, it) }
                        },
                        onHideQuest = viewModel.bottomSheet::hideQuest,
                        isSurvey = viewModel.bottomSheet::isSurvey,
                        onSubmitEdit = viewModel.bottomSheet::submitEdit,
                        onCommentNote = viewModel.bottomSheet::commentNote,
                        onCreateNote = viewModel.bottomSheet::createNote,
                        shownBottomSheet = shownBottomSheet,
                        mapRotation = mapCamera.bearing.toFloat(),
                        mapTilt = mapCamera.tilt.toFloat(),
                        mapPosition = mapPosition,
                        mapMetersPerDp = metersPerDp,
                        onSetMapMarkers = { if (id == sheet.id) sheet.formMarkers = it?.toList() },
                        formState = formState,
                        lastMapClick = sheet.lastMapClick,
                    )
                }
            }
        }
    }

    lastQuestSolved?.let { LastQuestSolvedEffect(it) }
    MapContextMenu(
        expanded = showMapContextMenu,
        onDismissRequest = { showMapContextMenu = false },
        onClickCreateNote = {
            if (mapState.cameraPosition.zoom < ApplicationConstants.NOTE_MIN_ZOOM) showToast = Toast.ImpreciseNote
            else lastLongPress?.let { composeNote(it.position) }
        },
        onClickCreateTrack = { tracks.startRecording() },
        isOpenLocationAvailable = mapAppLauncher.isAvailable(),
        onClickOpenLocation = {
            lastLongPress?.let { mapAppLauncher.openAt(it.position, mapState.cameraPosition.zoom) }
        },
        offset = lastLongPress?.screenOffset ?: DpOffset.Zero,
    )
    if (showLocationPermissionRationaleDialog) {
        ConfirmationDialog(
            onDismissRequest = { showLocationPermissionRationaleDialog = false },
            onConfirmed = { locationProvider.requestPermission() },
            title = { Text(stringResource(Res.string.no_location_permission_warning_title)) },
            text = { Text(stringResource(Res.string.no_location_permission_warning)) },
        )
    }
    if (showApplicationSettingsDialog) {
        ConfirmationDialog(
            onDismissRequest = { showApplicationSettingsDialog = false },
            onConfirmed = { systemSettingsLauncher.openApplicationSettings() },
            text = { Text(stringResource(Res.string.turn_on_location_request)) },
        )
    }
    if (showLocationSettingsDialog) {
        ConfirmationDialog(
            onDismissRequest = { showLocationSettingsDialog = false },
            onConfirmed = { systemSettingsLauncher.openLocationServicesSettings() },
            text = { Text(stringResource(Res.string.turn_on_location_request)) },
        )
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
            onClickDownload = ::onClickDownload,
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

    if (confirmReplaceDownload) {
        ConfirmReplaceDownloadDialog(
            onDismissRequest = { confirmReplaceDownload = false },
            onConfirmed = { download() }
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
            onFinished = { viewModel.hasShownTutorial = true },
        )
    }
    //endregion
}

/** A bottom sheet as shown, with its form's state */
private data class ShownForm(val id: String, val sheet: ShownBottomSheet, val formState: BottomSheetFormState)

private enum class Toast {
    Offline,
    TeamModeActive,
    TeamModeDeactivated,
    DownloadAreaTooBig,
    NoEmailClient,
    CannotFindBounds,
    NoLocation,
    ImpreciseNote
}

private val Toast.messageResource: StringResource get() =  when (this) {
    Toast.CannotFindBounds -> Res.string.cannot_find_bbox_or_reduce_tilt
    Toast.NoLocation -> Res.string.no_gps_no_quests
    Toast.ImpreciseNote -> Res.string.create_new_note_unprecise
    Toast.Offline -> Res.string.offline
    Toast.TeamModeActive -> Res.string.team_mode_active
    Toast.TeamModeDeactivated -> Res.string.team_mode_deactivated
    Toast.DownloadAreaTooBig -> Res.string.download_area_too_big
    Toast.NoEmailClient -> Res.string.no_email_client
}
