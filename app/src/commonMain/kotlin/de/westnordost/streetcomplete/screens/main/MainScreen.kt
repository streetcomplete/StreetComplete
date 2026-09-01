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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.location.SurveyChecker
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MainBottomSheet
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.controls.MainScreenControls
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistorySidebar
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.icon
import de.westnordost.streetcomplete.screens.main.errors.LastCrashEffect
import de.westnordost.streetcomplete.screens.main.errors.LastDownloadErrorEffect
import de.westnordost.streetcomplete.screens.main.errors.LastUploadErrorEffect
import de.westnordost.streetcomplete.screens.main.messages.MessageDialog
import de.westnordost.streetcomplete.screens.main.map.MainMap
import de.westnordost.streetcomplete.screens.main.map.MainMapDownloadArea
import de.westnordost.streetcomplete.screens.main.map.MainMapPinMode
import de.westnordost.streetcomplete.screens.main.map.MainMapState
import de.westnordost.streetcomplete.screens.main.map.calculateMainMapDownloadArea
import de.westnordost.streetcomplete.screens.main.map.getMainMapHighlightedElementMarkers
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapState
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.main.urlconfig.ApplyUrlConfigEffect
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.dialogs.ConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import org.maplibre.compose.location.HeadingProvider
import org.maplibre.compose.location.HeadingRequest
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationPermission
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.compose.location.SystemSettingsLauncher
import org.maplibre.compose.location.rememberDefaultHeadingProvider
import org.maplibre.compose.location.rememberDefaultLocationProvider
import org.maplibre.compose.location.rememberSystemSettingsLauncher
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.DMS
import kotlin.time.Duration.Companion.milliseconds

/** Complete shared main map and its Compose UI. */
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    editHistoryViewModel: EditHistoryViewModel,
    mainBottomSheetViewModel: MainBottomSheetViewModel,
    onClickSettings: () -> Unit,
    onClickQuestSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickProfile: () -> Unit,
    onClickLogin: () -> Unit,
    modifier: Modifier = Modifier,
    mapState: MainMapState = rememberMainMapState(),
    locationProvider: LocationProvider = rememberDefaultLocationProvider(),
    headingProvider: HeadingProvider = rememberDefaultHeadingProvider(),
    systemSettingsLauncher: SystemSettingsLauncher = rememberSystemSettingsLauncher(),
    mapAppLauncher: MapAppLauncher = koinInject(),
    surveyChecker: SurveyChecker = koinInject(),
    mapDataSource: MapDataWithEditsSource = koinInject(),
    featureDictionary: Lazy<FeatureDictionary> = koinInject(named("FeatureDictionaryLazy")),
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
    val geoUri by viewModel.geoUri.collectAsState()
    val lastCrashReport by viewModel.lastCrashReport.collectAsState()
    val lastDownloadError by viewModel.lastDownloadError.collectAsState()
    val lastUploadError by viewModel.lastUploadError.collectAsState()

    var latestLocationEvent by remember { mutableStateOf<LocationEvent?>(null) }
    val locationPermission by locationProvider.permission.collectAsState()
    val headingUpdates = remember(headingProvider) {
        headingProvider.updates(HeadingRequest(33.milliseconds))
    }
    val heading by headingUpdates.collectAsState(initial = null)
    val mapCamera = mapState.cameraPosition
    val mapPosition = mapCamera.target.toLatLon()
    val metersPerDp = mapState.metersPerDp
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val windowInfo = LocalWindowInfo.current
    val openFormPadding = Dimensions.getOpenQuestFormMapPadding(windowInfo)
    val displayedPosition = mapState.displayedLocation?.position?.let(mapState::offsetOf)?.let {
        with(density) { Offset(it.x.toPx(), it.y.toPx()) }
    }
    val locationState = getLocationState(locationPermission, latestLocationEvent)
    val locationRotation = heading
        ?.bearing
        ?.clockwiseRotationTo(Bearing.North)
        ?.toDouble(DMS.Degrees)
        ?.minus(mapCamera.bearing)
        ?.toFloat()

    val showZoomButtons by viewModel.showZoomButtons.collectAsState()

    val isRequestingLogin by viewModel.isRequestingLogin.collectAsState()

    val showEditHistorySidebar by editHistoryViewModel.isShowingSidebar.collectAsState()

    val editItems by editHistoryViewModel.editItems.collectAsState()
    val selectedEdit by editHistoryViewModel.selectedEdit.collectAsState()
    val hasEdits by remember { derivedStateOf { editItems.isNotEmpty() } }

    val shownBottomSheet by mainBottomSheetViewModel.shownBottomSheet.collectAsState()
    val geometryOffsetInWindow by mainBottomSheetViewModel.geometryOffsetInWindow.collectAsState()

    var confirmReplaceDownload by remember { mutableStateOf(false) }
    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }
    var showToast by remember { mutableStateOf<Toast?>(null) }
    var locationDialog by remember { mutableStateOf<LocationDialog?>(null) }
    var lastMapClick by remember { mutableStateOf<MapClick?>(null) }
    var lastMapLongPress by remember { mutableStateOf<Pair<DpOffset, LatLon>?>(null) }
    var showMapContextMenu by remember { mutableStateOf(false) }
    var lastQuestSolved by remember { mutableStateOf<QuestSolvedEvent?>(null) }
    var wasFollowingPosition by remember { mutableStateOf<Boolean?>(null) }
    var wasNavigationMode by remember { mutableStateOf<Boolean?>(null) }

    fun freezeMap() {
        if (wasFollowingPosition == null) wasFollowingPosition = mapState.isFollowingPosition
        if (wasNavigationMode == null) wasNavigationMode = mapState.isNavigationMode
        mapState.setFollowingPosition(false)
        mapState.setNavigationMode(false)
    }

    fun unfreezeMap() {
        wasFollowingPosition?.let(mapState::setFollowingPosition)
        wasNavigationMode?.let(mapState::setNavigationMode)
        wasFollowingPosition = null
        wasNavigationMode = null
    }

    fun getCrosshairPosition(): LatLon? {
        val size = windowInfo.containerDpSize
        val left = openFormPadding.calculateLeftPadding(layoutDirection)
        val right = openFormPadding.calculateRightPadding(layoutDirection)
        val top = openFormPadding.calculateTopPadding()
        val bottom = openFormPadding.calculateBottomPadding()
        return mapState.positionAt(
            DpOffset(
                x = left + (size.width - left - right) / 2,
                y = top + (size.height - top - bottom) / 2,
            )
        )
    }

    fun downloadVisibleArea() {
        when (val area = calculateMainMapDownloadArea(mapState.displayedArea, mapPosition)) {
            is MainMapDownloadArea.Available -> viewModel.download(area.bounds)
            MainMapDownloadArea.DisplayAreaUnavailable -> showToast = Toast.DownloadAreaUnavailable
            MainMapDownloadArea.TooLarge -> showToast = Toast.DownloadAreaTooBig
        }
    }

    fun onClickDownload() {
        if (viewModel.isConnected) {
            if (viewModel.isUserInitiatedDownloadInProgress) {
                confirmReplaceDownload = true
            } else {
                downloadVisibleArea()
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

    LaunchedEffect(locationProvider) {
        locationProvider.updates(LocationRequest()).collect { event ->
            latestLocationEvent = event
            if (event is LocationEvent.Update) {
                surveyChecker.addRecentLocation(event.measurement.toLocation())
            }
        }
    }

    LaunchedEffect(geoUri) {
        geoUri?.let { camera ->
            viewModel.consumeGeoUri()
            mapState.moveTo(
                position = camera.position,
                zoom = camera.zoom,
                padding = PaddingValues(0.dp),
            )
        }
    }

    LaunchedEffect(showEditHistorySidebar) {
        if (showEditHistorySidebar) {
            freezeMap()
            mapState.hideOverlay()
            mapState.setPinMode(MainMapPinMode.EDITS)
        } else {
            unfreezeMap()
            mapState.clearFocus()
            mapState.clearHighlighting()
            mapState.setPinMode(MainMapPinMode.QUESTS)
        }
    }

    LaunchedEffect(selectedEdit, showEditHistorySidebar) {
        val edit = selectedEdit
        if (edit != null) {
            val geometry = editHistoryViewModel.getEditGeometry(edit)
            mapState.startFocus(geometry)
            mapState.showGeometry(geometry)
            edit.icon?.let { mapState.selectPins(it, listOf(edit.position)) }
            mapState.hideOverlay()
        } else if (showEditHistorySidebar) {
            mapState.clearFocus()
            mapState.clearHighlighting()
            mapState.hideOverlay()
        }
    }

    LaunchedEffect(shownBottomSheet) {
        val bottomSheet = shownBottomSheet
        if (bottomSheet == null) {
            mapState.clearHighlighting()
            unfreezeMap()
            mapState.endFocus()
            return@LaunchedEffect
        }

        freezeMap()
        when (bottomSheet) {
            is ShownBottomSheet.CreateOsmNote -> Unit
            is ShownBottomSheet.OsmNoteQuest -> {
                mapState.startFocus(bottomSheet.quest.geometry, openFormPadding)
                mapState.showGeometry(bottomSheet.quest.geometry)
                mapState.selectPins(bottomSheet.quest.type.icon, bottomSheet.quest.markerLocations)
                mapState.hidePins()
                mapState.hideOverlay()
            }
            is ShownBottomSheet.OsmQuest -> {
                val quest = bottomSheet.quest
                mapState.startFocus(quest.geometry, openFormPadding)
                mapState.showGeometry(quest.geometry)
                mapState.selectPins(quest.type.icon, quest.markerLocations)
                mapState.hidePins()
                mapState.hideOverlay()
                mapState.setMarkers(
                    kotlinx.coroutines.withContext(Dispatchers.IO) {
                        getMainMapHighlightedElementMarkers(
                            quest,
                            bottomSheet.element,
                            mapDataSource,
                            featureDictionary.value,
                        )
                    }
                )
            }
            is ShownBottomSheet.Overlay -> {
                val geometry = bottomSheet.geometry
                if (geometry == null) {
                    getCrosshairPosition()?.let { position ->
                        mapState.moveTo(position, padding = openFormPadding)
                    }
                } else {
                    mapState.moveTo(mapPosition, padding = openFormPadding)
                    mapState.showGeometry(geometry)
                    mapState.selectPins(bottomSheet.overlay.icon, listOf(geometry.center))
                }
                mapState.hidePins()
            }
        }
    }

    LaunchedEffect(shownBottomSheet?.position, mapCamera, mapState.mapState.presentation) {
        mainBottomSheetViewModel.geometryOffsetInWindow.value =
            shownBottomSheet?.position?.let(mapState::offsetOf)?.let {
                with(density) { Offset(it.x.toPx(), it.y.toPx()) }
            }
    }

    LaunchedEffect(selectedOverlay) {
        if (shownBottomSheet is ShownBottomSheet.Overlay) {
            mainBottomSheetViewModel.closeBottomSheet()
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
        MainMap(
            onClickOverlayElement = { elementKey ->
                val overlay = selectedOverlay
                if (overlay != null && shownBottomSheet == null) {
                    mainBottomSheetViewModel.showElementInOverlay(overlay, elementKey)
                }
            },
            onClickQuest = { questKey ->
                if (shownBottomSheet == null) mainBottomSheetViewModel.showQuest(questKey)
            },
            onClickEdit = editHistoryViewModel::select,
            onClickMap = { position, clickRadiusInMeters ->
                when {
                    shownBottomSheet != null -> {
                        lastMapClick = MapClick(position, clickRadiusInMeters)
                    }
                    showEditHistorySidebar -> editHistoryViewModel.hideSidebar()
                }
            },
            onLongPress = { offset, position ->
                if (shownBottomSheet == null && !showEditHistorySidebar) {
                    lastMapLongPress = offset to position
                    showMapContextMenu = true
                }
            },
            locationEvent = latestLocationEvent,
            locationRotation = locationRotation,
            modifier = Modifier.fillMaxSize(),
            state = mapState,
        )

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
                onClickZoomIn = mapState::zoomIn,
                onClickZoomOut = mapState::zoomOut,
                onZoomDrag = mapState::zoomByDrag,

                mapRotation = mapCamera.bearing.toFloat(),
                mapTilt = mapCamera.tilt.toFloat(),
                onClickCompass = mapState::resetCompass,

                locationState = locationState,
                isNavigationMode = mapState.isNavigationMode,
                isFollowingPosition = mapState.isFollowingPosition,
                displayedLocationOffset = displayedPosition,
                onClickLocation = {
                    when (val permission = locationPermission) {
                        is LocationPermission.NotGranted -> {
                            when {
                                permission.canRequest != false && !permission.shouldShowRationale ->
                                    locationProvider.requestPermission()
                                permission.canRequest != false ->
                                    locationDialog = LocationDialog.PermissionRationale
                                systemSettingsLauncher.canOpenApplicationSettings ->
                                    locationDialog = LocationDialog.ApplicationSettings
                                else -> showToast = Toast.NoLocation
                            }
                        }
                        is LocationPermission.Granted -> when {
                            locationState == LocationState.ALLOWED -> {
                                if (systemSettingsLauncher.canOpenLocationServicesSettings) {
                                    locationDialog = LocationDialog.LocationServices
                                } else {
                                    showToast = Toast.NoLocation
                                }
                            }
                            !mapState.isFollowingPosition -> mapState.setFollowingPosition(true)
                            else -> mapState.setNavigationMode(!mapState.isNavigationMode)
                        }
                    }
                },
                onClickLocationPointer = { mapState.setFollowingPosition(true) },

                isRecordingTracks = mapState.isRecordingTrack,
                onClickStopTrackRecording = {
                    val track = mapState.stopTrackRecording()
                    mapState.displayedLocation?.position?.let { position ->
                        mainBottomSheetViewModel.showCreateNote(track.takeIf { it.isNotEmpty() })
                        mapState.moveTo(position, padding = openFormPadding)
                    }
                },

                isCreateNodeEnabled = isCreateNodeEnabled,
                onClickCreate = {
                    if (mapCamera.zoom >= 17.0) {
                        selectedOverlay?.let(mainBottomSheetViewModel::showCreateElementInOverlay)
                    } else {
                        showToast = Toast.DownloadAreaTooBig
                    }
                },

                hasEdits = hasEdits,
                isUndoEnabled = !isUploadingOrDownloading,
                onClickUndo = { editHistoryViewModel.showSidebar() },

                metersPerDp = metersPerDp,
                userHasMovedMap = mapState.userHasMovedCamera,
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
                getEditElement = editHistoryViewModel::getEditElement,
            )
        }

        AnimatedContent(
            targetState = shownBottomSheet,
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
        ) { shownBottomSheet ->
            if (shownBottomSheet != null) {
                MainBottomSheet(
                    onDismiss = { mainBottomSheetViewModel.closeBottomSheet() },
                    onSolved = { icon, position ->
                        mapState.offsetOf(position)?.let { offset ->
                            lastQuestSolved = QuestSolvedEvent(
                                icon,
                                with(density) { Offset(offset.x.toPx(), offset.y.toPx()) },
                            )
                        }
                    },
                    viewModel = mainBottomSheetViewModel,
                    shownBottomSheet = shownBottomSheet,
                    geometryOffsetInWindow = geometryOffsetInWindow,
                    mapRotation = mapCamera.bearing.toFloat(),
                    mapTilt = mapCamera.tilt.toFloat(),
                    mapPosition = mapPosition,
                    mapMetersPerDp = metersPerDp,
                    onSetMapMarkers = mapState::setMarkers,
                    getOffset = { position ->
                        mapState.offsetOf(position)?.let { offset ->
                            with(density) { Offset(offset.x.toPx(), offset.y.toPx()) }
                        }
                    },
                    lastMapClick = lastMapClick,
                )
            }
        }

        lastQuestSolved?.let { LastQuestSolvedEffect(it) }

        val longPress = lastMapLongPress
        MapContextMenu(
            expanded = showMapContextMenu,
            onDismissRequest = { showMapContextMenu = false },
            onClickCreateNote = {
                longPress?.second?.let { position ->
                    if (mapCamera.zoom < ApplicationConstants.NOTE_MIN_ZOOM) {
                        showToast = Toast.CreateNoteTooImprecise
                    } else {
                        mainBottomSheetViewModel.showCreateNote(null)
                        mapState.moveTo(position, padding = openFormPadding)
                    }
                }
            },
            onClickCreateTrack = mapState::startTrackRecording,
            onClickOpenLocation = {
                longPress?.second?.let { mapAppLauncher.openAt(it, mapCamera.zoom) }
            },
            isOpenLocationAvailable = remember(mapAppLauncher) { mapAppLauncher.isAvailable() },
            offset = longPress?.first ?: DpOffset.Zero,
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
            onConfirmed = ::downloadVisibleArea,
        )
    }

    locationDialog?.let { dialog ->
        ConfirmationDialog(
            onDismissRequest = { locationDialog = null },
            onConfirmed = {
                when (dialog) {
                    LocationDialog.PermissionRationale -> locationProvider.requestPermission()
                    LocationDialog.ApplicationSettings -> systemSettingsLauncher.openApplicationSettings()
                    LocationDialog.LocationServices -> systemSettingsLauncher.openLocationServicesSettings()
                }
            },
            title = if (dialog == LocationDialog.PermissionRationale) {
                { Text(stringResource(Res.string.no_location_permission_warning_title)) }
            } else {
                null
            },
            text = {
                Text(
                    stringResource(
                        if (dialog == LocationDialog.PermissionRationale) {
                            Res.string.no_location_permission_warning
                        } else {
                            Res.string.turn_on_location_request
                        }
                    )
                )
            },
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
}

private enum class Toast {
    Offline,
    TeamModeActive,
    TeamModeDeactivated,
    DownloadAreaUnavailable,
    DownloadAreaTooBig,
    CreateNoteTooImprecise,
    NoEmailClient,
    NoLocation,
}

private enum class LocationDialog { PermissionRationale, ApplicationSettings, LocationServices }

private val Toast.messageResource: StringResource get() = when (this) {
    Toast.Offline -> Res.string.offline
    Toast.TeamModeActive -> Res.string.team_mode_active
    Toast.TeamModeDeactivated -> Res.string.team_mode_deactivated
    Toast.DownloadAreaUnavailable -> Res.string.cannot_find_bbox_or_reduce_tilt
    Toast.DownloadAreaTooBig -> Res.string.download_area_too_big
    Toast.CreateNoteTooImprecise -> Res.string.create_new_note_unprecise
    Toast.NoEmailClient -> Res.string.no_email_client
    Toast.NoLocation -> Res.string.no_gps_no_quests
}

private fun getLocationState(
    permission: LocationPermission,
    event: LocationEvent?,
): LocationState? {
    if (permission is LocationPermission.NotGranted) return LocationState.DENIED
    return when (event) {
        null -> LocationState.ENABLED
        is LocationEvent.Update -> LocationState.UPDATING
        is LocationEvent.Unavailable -> when (event.reason) {
            LocationUnavailableReason.ServicesDisabled -> LocationState.ALLOWED
            LocationUnavailableReason.TemporarilyUnavailable -> LocationState.SEARCHING
            LocationUnavailableReason.PermissionDenied -> LocationState.DENIED
            LocationUnavailableReason.Unsupported,
            LocationUnavailableReason.Misconfigured,
            LocationUnavailableReason.UnexpectedFailure -> null
        }
    }
}

