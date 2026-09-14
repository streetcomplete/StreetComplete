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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MainBottomSheet
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.controls.MainScreenControls
import de.westnordost.streetcomplete.screens.main.controls.PointerPinButton
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistorySidebar
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.errors.LastCrashEffect
import de.westnordost.streetcomplete.screens.main.errors.LastDownloadErrorEffect
import de.westnordost.streetcomplete.screens.main.errors.LastUploadErrorEffect
import de.westnordost.streetcomplete.screens.main.map.BASE_STYLE
import de.westnordost.streetcomplete.screens.main.map.MainMap
import de.westnordost.streetcomplete.screens.main.map.MainMapContent
import de.westnordost.streetcomplete.screens.main.map.MainMapViewModel
import de.westnordost.streetcomplete.screens.main.map.getTrackBearing
import de.westnordost.streetcomplete.screens.main.map.layers.Pin
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapCameraState
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapTrackState
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.screens.main.map.toStreetCompleteBoundingBox
import de.westnordost.streetcomplete.screens.main.map.zoomToCluster
import de.westnordost.streetcomplete.screens.main.messages.MessageDialog
import de.westnordost.streetcomplete.screens.main.teammode.TeamModeWizard
import de.westnordost.streetcomplete.screens.main.urlconfig.ApplyUrlConfigEffect
import de.westnordost.streetcomplete.screens.tutorial.IntroTutorialScreen
import de.westnordost.streetcomplete.screens.tutorial.OverlaysTutorialScreen
import de.westnordost.streetcomplete.ui.common.AnimatedScreenVisibility
import de.westnordost.streetcomplete.ui.common.ToastPopup
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.dir
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toLocation
import de.westnordost.streetcomplete.util.ktx.updatesWithPermissionChanges
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.location.HeadingMeasurement
import org.maplibre.compose.location.HeadingProvider
import org.maplibre.compose.location.HeadingRequest
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationMeasurement
import org.maplibre.compose.location.LocationPermission
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.compose.location.SystemSettingsLauncher
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.attributions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.extensions.inDegrees
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

/** The map and its controls, forms, and sidebars. */
@Composable
fun MainScreen(
    onClickSettings: () -> Unit,
    onClickQuestSettings: () -> Unit,
    onClickAbout: () -> Unit,
    onClickProfile: () -> Unit,
    onClickLogin: () -> Unit,
    modifier: Modifier = Modifier,
    uri: String? = null,
    onConsumedUri: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel(),
    editHistoryViewModel: EditHistoryViewModel = koinViewModel(),
    mainBottomSheetViewModel: MainBottomSheetViewModel = koinViewModel(),
    mapViewModel: MainMapViewModel = koinViewModel(),
    runtime: MapRuntime = koinInject(),
    locationProvider: LocationProvider = koinInject(),
    headingProvider: HeadingProvider = koinInject(),
    systemSettingsLauncher: SystemSettingsLauncher = koinInject(),
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

    val showZoomButtons by viewModel.showZoomButtons.collectAsState()

    val isRequestingLogin by viewModel.isRequestingLogin.collectAsState()

    val emailAppLauncher = rememberEmailAppLauncher()

    var confirmReplaceDownload by remember { mutableStateOf(false) }
    var showOverlaysTutorial by remember { mutableStateOf(false) }
    var showIntroTutorial by remember { mutableStateOf(false) }
    var showTeamModeWizard by remember { mutableStateOf(false) }
    var showMainMenuDialog by remember { mutableStateOf(false) }
    var shownMessage by remember { mutableStateOf<Message?>(null) }
    var showToast by remember { mutableStateOf<Toast?>(null) }

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val windowInfo = LocalWindowInfo.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val sheet = rememberMainSheetState(mainBottomSheetViewModel)
    val editHistory = rememberEditHistoryState(editHistoryViewModel)
    var displayedLocation by rememberSerializable { mutableStateOf<LocationMeasurement?>(null) }
    var heading by remember { mutableStateOf<HeadingMeasurement?>(null) }
    var locationState by remember { mutableStateOf<LocationState?>(LocationState.ENABLED) }
    var userHasMovedCamera by rememberSaveable { mutableStateOf(false) }
    var lastLongPress by remember { mutableStateOf<Pair<DpOffset, LatLon>?>(null) }
    var showMapContextMenu by remember { mutableStateOf(false) }
    var lastQuestSolved by remember { mutableStateOf<QuestSolvedEvent?>(null) }
    var mapOrigin by remember { mutableStateOf(Offset.Zero) }
    var locationDialog by remember { mutableStateOf<LocationDialog?>(null) }
    val tracks = rememberMainMapTrackState()
    val downloadedTiles by mapViewModel.downloadedTiles.collectAsState()
    val geoUri by viewModel.geoUri.collectAsState()
    val mapAppLauncher = rememberMapAppLauncher()

    val shownBottomSheet = sheet.shownBottomSheet
    val selectedEdit = editHistory.selectedEdit
    val highlightedMarkers by produceState<List<Marker>>(emptyList(), shownBottomSheet) {
        value = shownBottomSheet?.let { mainBottomSheetViewModel.getHighlightedMarkers(it) }.orEmpty()
    }
    val markers = sheet.formMarkers ?: highlightedMarkers
    // the overlay is hidden behind quest forms because these highlight other elements, and behind
    // the edit history because it should look clean
    val showOverlay = selectedOverlay != null &&
        shownBottomSheet !is ShownBottomSheet.OsmQuest &&
        shownBottomSheet !is ShownBottomSheet.OsmNoteQuest &&
        !editHistory.isShowing

    val initialCamera = remember(viewModel) { viewModel.initialCamera }
    val mapState = rememberMapState(runtime, BaseStyle.Json(BASE_STYLE), initialCameraPosition = initialCamera) {
        val state = checkNotNull(LocalMapState.current)
        val showPinsAtZoom by remember(state) { derivedStateOf { state.cameraPosition.zoom >= 13 } }
        val showOverlayAtZoom by remember(state) { derivedStateOf { state.cameraPosition.zoom >= 14 } }
        // Quest pins and overlay data stay loaded while hidden behind a form, so that closing it
        // does not reload them. The edit history is loaded only while its sidebar is shown.
        val questPins by mapViewModel.questPins.collectAsState()
        val styledElements by mapViewModel.styleableElements.collectAsState()
        val pins: Collection<Pin>
        val onClickPin: (JsonObject) -> ClickResult
        if (editHistory.isShowing) {
            pins = if (showPinsAtZoom) mapViewModel.editHistoryPins.collectAsState().value else emptyList()
            onClickPin = { properties ->
                val key = mapViewModel.getEditKey(properties)
                if (key == null) ClickResult.Pass else {
                    editHistory.select(key)
                    ClickResult.Consume
                }
            }
        } else if (!sheet.isOpen || sheet.selection is MainBottomSheetSelection.CreateNote) {
            pins = if (showPinsAtZoom) questPins else emptyList()
            onClickPin = { properties ->
                val key = mapViewModel.getQuestKey(properties)
                if (key == null || sheet.isOpen) ClickResult.Pass else {
                    sheet.show(MainBottomSheetSelection.Quest(key))
                    ClickResult.Consume
                }
            }
        } else {
            pins = emptyList()
            onClickPin = { ClickResult.Pass }
        }
        MainMapContent(
            location = displayedLocation,
            heading = heading?.let { (it.bearing - Bearing.North).inDegrees.toFloat() },
            isRecording = tracks.isRecording,
            trackpoints = tracks.currentTrackPositions,
            oldTrackpointsLists = tracks.previousTrackPositions,
            shownBottomSheet = shownBottomSheet,
            shownMarkers = markers,
            hiddenLayers = selectedOverlay?.hidesLayers.orEmpty(),
            showOverlay = showOverlay,
            selectedEdit = selectedEdit,
            highlightedGeometry = editHistory.highlightedGeometry,
            downloadedTiles = downloadedTiles,
            pins = pins,
            onClickPin = onClickPin,
            onClickCluster = { bounds -> scope.launch { state.zoomToCluster(bounds) } },
            styledElements = if (showOverlay && showOverlayAtZoom) styledElements else emptyList(),
            onClickElement = { properties ->
                val key = mapViewModel.getElementKey(properties)
                val overlay = selectedOverlay
                if (key == null || overlay == null || sheet.isOpen) ClickResult.Pass else {
                    sheet.show(MainBottomSheetSelection.Overlay(overlay.name, key))
                    ClickResult.Consume
                }
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
    val cameraPadding = if (sheet.isOpen) sheetPadding else PaddingValues(0.dp)
    val isNavigationMode = cameraState.isNavigationMode
    val isFollowingPosition = cameraState.isFollowingPosition
    val isRecordingTracks = tracks.isRecording

    fun getOffset(position: LatLon): Offset? = mapState.screenLocationFromPosition(position.toPosition())?.let {
        with(density) { Offset(it.x.toPx(), it.y.toPx()) } + mapOrigin
    }
    val geometryOffsetInWindow = remember(shownBottomSheet, viewport, mapCamera, mapOrigin) {
        shownBottomSheet?.position?.let(::getOffset)
    }
    fun followLocation() {
        cameraState.isFollowingPosition = true
        scope.launch { cameraState.followLocation(displayedLocation?.position?.toLatLon(), getTrackBearing(tracks.currentTrack)) }
    }
    fun zoomBy(amount: Double) {
        scope.launch { mapState.animateCameraPosition(mapState.cameraPosition.copy(zoom = mapState.cameraPosition.zoom + amount), 300.milliseconds) }
    }
    fun composeNote(position: LatLon, trackpoints: List<Trackpoint>? = null) {
        sheet.show(MainBottomSheetSelection.CreateNote(position, trackpoints))
    }
    fun download() {
        val bounds = mapState.viewport?.visibleBounds?.toStreetCompleteBoundingBox()
        if (bounds == null) {
            showToast = Toast.CannotFindBounds
            return
        }
        val tilesBounds = bounds.asBoundingBoxOfEnclosingTiles(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        val area = tilesBounds.area() / 1_000_000
        if (area > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
            showToast = Toast.DownloadAreaTooBig
            return
        }
        val downloadBounds = if (area < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM) {
            val radius = sqrt(1_000_000 * ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM / PI)
            mapState.cameraPosition.target.toLatLon().enclosingBoundingBox(radius)
        } else tilesBounds
        viewModel.download(downloadBounds)
    }
    fun clickLocation() {
        val permission = locationProvider.permission.value
        if (permission is LocationPermission.NotGranted) {
            when {
                permission.canRequest == false -> {
                    if (systemSettingsLauncher.canOpenApplicationSettings) locationDialog = LocationDialog.ApplicationSettings
                    else showToast = Toast.NoLocation
                }
                permission.shouldShowRationale -> locationDialog = LocationDialog.PermissionRationale
                else -> locationProvider.requestPermission()
            }
        } else when {
            locationState == LocationState.ALLOWED -> {
                if (systemSettingsLauncher.canOpenLocationServicesSettings) locationDialog = LocationDialog.LocationSettings
                else showToast = Toast.NoLocation
            }
            !cameraState.isFollowingPosition -> followLocation()
            else -> scope.launch {
                cameraState.setNavigationMode(!cameraState.isNavigationMode,
                    displayedLocation?.position?.toLatLon(), getTrackBearing(tracks.currentTrack))
            }
        }
    }

    LaunchedEffect(uri) {
        if (uri != null) {
            viewModel.setUri(uri)
            onConsumedUri()
        }
    }
    LaunchedEffect(geoUri) {
        geoUri?.let {
            cameraState.moveTo(it)
            viewModel.consumeGeoUri()
        }
    }
    // Apply the MapLibre viewport to StreetComplete's quest and overlay data sources.
    LaunchedEffect(mapState, mapViewModel) {
        snapshotFlow { mapState.cameraPosition.zoom to mapState.viewport?.visibleBounds }
            .collect { (zoom, bounds) -> mapViewModel.onViewportChanged(zoom, bounds?.toStreetCompleteBoundingBox()) }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.saveCamera(mapState.cameraPosition, cameraState.isFollowingPosition, cameraState.isNavigationMode)
    }
    LaunchedEffect(locationProvider, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            locationProvider.updatesWithPermissionChanges(LocationRequest()).collect { event ->
                when (event) {
                    is LocationEvent.Update -> {
                        val fix = event.toLocation()
                        // Survey checking receives every fix, including ones too inaccurate for a track.
                        mapViewModel.onLocationChanged(fix)
                        displayedLocation = event.measurement
                        locationState = LocationState.UPDATING
                        tracks.addLocation(event.measurement)
                        launch { cameraState.followLocation(fix.position, getTrackBearing(tracks.currentTrack)) }
                    }
                    is LocationEvent.Unavailable -> {
                        locationState = when (event.reason) {
                            LocationUnavailableReason.ServicesDisabled -> LocationState.ALLOWED
                            LocationUnavailableReason.TemporarilyUnavailable -> LocationState.SEARCHING
                            LocationUnavailableReason.PermissionDenied -> LocationState.DENIED
                            LocationUnavailableReason.Unsupported, LocationUnavailableReason.UnexpectedFailure -> null
                        }
                        displayedLocation = null
                        tracks.clear()
                        launch { cameraState.setNavigationMode(false, null, null) }
                    }
                }
            }
        }
    }
    LaunchedEffect(headingProvider, lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            headingProvider.updates(HeadingRequest(33.milliseconds)).collect { heading = it }
        }
    }
    // The camera stops following the user's location while a form or the edit history is open and
    // moves to the selected object once. Closing a form moves it back, closing the edit history
    // does not.
    val mapMode = when {
        sheet.isOpen -> MapMode.Sheet(sheet.id)
        editHistory.isShowing -> MapMode.EditHistory(editHistory.selectedEditKey)
        else -> MapMode.Free
    }
    // Saved, so that the camera does not move again after the process was recreated
    var focusedMode by rememberSerializable { mutableStateOf<MapMode?>(null) }
    LaunchedEffect(cameraState, mapMode) {
        val focus = focusedMode != mapMode
        focusedMode = mapMode
        when (mapMode) {
            is MapMode.Sheet -> {
                cameraState.freeze()
                if (!focus) return@LaunchedEffect
                val shown = snapshotFlow { sheet.shownBottomSheet }.filterNotNull().first()
                when (val selection = sheet.selection) {
                    is MainBottomSheetSelection.CreateNote -> mapState.animateCameraPosition(
                        mapState.cameraPosition.copy(target = selection.position.toPosition()), 300.milliseconds,
                    )
                    else -> when (shown) {
                        is ShownBottomSheet.OsmQuest -> cameraState.focus(shown.quest.geometry, restorable = true)
                        is ShownBottomSheet.OsmNoteQuest -> cameraState.focus(shown.quest.geometry, restorable = true)
                        else -> Unit
                    }
                }
            }
            is MapMode.EditHistory -> {
                cameraState.freeze()
                if (!focus || mapMode.selectedEditKey == null) return@LaunchedEffect
                val edit = snapshotFlow { editHistory.selectedEdit }.filterNotNull().first()
                cameraState.focus(editHistoryViewModel.getEditGeometry(edit), restorable = false)
            }
            MapMode.Free -> {
                cameraState.unfreeze(displayedLocation?.position?.toLatLon(), getTrackBearing(tracks.currentTrack))
                cameraState.endFocus()
            }
        }
    }
    LaunchedEffect(selectedOverlay) {
        val selection = sheet.selection as? MainBottomSheetSelection.Overlay
        if (selection != null && selection.name != selectedOverlay?.name) sheet.close()
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
            state = mapState,
            modifier = Modifier.fillMaxSize().onGloballyPositioned { mapOrigin = it.positionInWindow() },
            cameraPadding = cameraPadding,
            onPan = { cameraState.onPan(displayedLocation != null) },
            onUserCameraMove = { userHasMovedCamera = true },
            onMapClick = { event ->
                val position = event.position?.toLatLon()
                if (sheet.isOpen && position != null) {
                    // forms react to clicks near the click position, e.g. to suggest a name
                    sheet.lastMapClick = MapClick(position, metersPerDp * 14)
                } else if (editHistory.isShowing) {
                    editHistory.hide()
                }
                ClickResult.Consume
            },
            onMapLongClick = { event ->
                val position = event.position?.toLatLon()
                if (!sheet.isOpen && !editHistory.isShowing && position != null) {
                    lastLongPress = event.screenOffset to position
                    showMapContextMenu = true
                }
                ClickResult.Consume
            },
            overlay = {
                if (!showIntroTutorial) {
                    displayedLocation?.position?.let { position ->
                        PointerPinButton(targetPosition = position, onClick = ::followLocation) {
                            Image(painterResource(Res.drawable.location_dot_small), null)
                        }
                    }
                }
            },
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
                    if (sheet.selection is MainBottomSheetSelection.Overlay) sheet.close()
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

                locationState = locationState,
                isNavigationMode = isNavigationMode,
                isFollowingPosition = isFollowingPosition,
                onClickLocation = ::clickLocation,

                isRecordingTracks = isRecordingTracks,
                onClickStopTrackRecording = {
                    tracks.stopRecording()
                    displayedLocation?.position?.toLatLon()?.let { composeNote(it, tracks.recordedTrack.takeIf { it.isNotEmpty() }) }
                },

                isCreateNodeEnabled = isCreateNodeEnabled,
                onClickCreate = {
                    if (mapCamera.zoom >= 17.0) {
                        selectedOverlay?.let { overlay ->
                            val size = windowInfo.containerDpSize
                            val left = sheetPadding.calculateLeftPadding(layoutDirection)
                            val right = sheetPadding.calculateRightPadding(layoutDirection)
                            val top = sheetPadding.calculateTopPadding()
                            val bottom = sheetPadding.calculateBottomPadding()
                            val position = mapState.positionFromScreenLocation(DpOffset(
                                left + (size.width - left - right) / 2,
                                top + (size.height - top - bottom) / 2,
                            ))
                            sheet.show(MainBottomSheetSelection.Overlay(overlay.name))
                            position?.let { mapState.setCameraPosition(mapState.cameraPosition.copy(target = it)) }
                        }
                    } else {
                        showToast = Toast.DownloadAreaTooBig
                    }
                },

                hasEdits = editHistory.hasEdits,
                isUndoEnabled = !isUploadingOrDownloading,
                onClickUndo = { editHistory.show() },

                metersPerDp = metersPerDp,
                attributions = mapState.style.attributions(),
                userHasMovedMap = userHasMovedCamera,
            )
        }

        val dir = LocalLayoutDirection.current.dir
        AnimatedVisibility(
            visible = editHistory.isShowing && editHistory.hasEdits,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it * dir }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it * dir }),
        ) {
            EditHistorySidebar(
                editItems = editHistory.editItems.orEmpty(),
                selectedEdit = selectedEdit,
                onSelectEdit = { editHistory.select(it.key) },
                onUndoEdit = { editHistory.undo(it.key) },
                onDismissRequest = editHistory::hide,
                getEditElement = editHistoryViewModel::getEditElement,
            )
        }

        AnimatedContent(
            targetState = shownBottomSheet?.let { sheet.id to it },
            contentKey = { it?.first },
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
                val (id, shownBottomSheet) = content
                sheet.formStateHolder.SaveableStateProvider(id) {
                    MainBottomSheet(
                        onDismiss = sheet::close,
                        onSolved = { icon, position ->
                            getOffset(position)?.let { lastQuestSolved = QuestSolvedEvent(icon, it) }
                        },
                        onHideQuest = mainBottomSheetViewModel::hideQuest,
                        isSurvey = mainBottomSheetViewModel::isSurvey,
                        onSubmitEdit = mainBottomSheetViewModel::submitEdit,
                        onCommentNote = mainBottomSheetViewModel::commentNote,
                        onCreateNote = mainBottomSheetViewModel::createNote,
                        shownBottomSheet = shownBottomSheet,
                        geometryOffsetInWindow = geometryOffsetInWindow,
                        mapRotation = mapCamera.bearing.toFloat(),
                        mapTilt = mapCamera.tilt.toFloat(),
                        mapPosition = mapCamera.target.toLatLon(),
                        mapMetersPerDp = metersPerDp,
                        onSetMapMarkers = { if (id == sheet.id) sheet.formMarkers = it.toList() },
                        getOffset = ::getOffset,
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
            else lastLongPress?.second?.let { composeNote(it) }
        },
        onClickCreateTrack = { tracks.startRecording() },
        isOpenLocationAvailable = mapAppLauncher.isAvailable(),
        onClickOpenLocation = {
            lastLongPress?.second?.let { mapAppLauncher.openAt(it, mapState.cameraPosition.zoom) }
        },
        offset = lastLongPress?.first ?: DpOffset.Zero,
    )
    locationDialog?.let { dialog ->
        AlertDialog(
            onDismissRequest = { locationDialog = null },
            title = if (dialog == LocationDialog.PermissionRationale) {
                { Text(stringResource(Res.string.no_location_permission_warning_title)) }
            } else null,
            text = { Text(stringResource(if (dialog == LocationDialog.PermissionRationale)
                Res.string.no_location_permission_warning else Res.string.turn_on_location_request)) },
            confirmButton = {
                TextButton(onClick = {
                    locationDialog = null
                    when (dialog) {
                        LocationDialog.PermissionRationale -> locationProvider.requestPermission()
                        LocationDialog.ApplicationSettings -> systemSettingsLauncher.openApplicationSettings()
                        LocationDialog.LocationSettings -> systemSettingsLauncher.openLocationServicesSettings()
                    }
                }) { Text(stringResource(Res.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { locationDialog = null }) { Text(stringResource(Res.string.cancel)) } },
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
}

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

private enum class LocationDialog { PermissionRationale, ApplicationSettings, LocationSettings }

/** What the map camera is bound to */
@Serializable
private sealed interface MapMode {
    /** Identified by the sheet id, since the selection itself may hold a long recorded track */
    @Serializable data class Sheet(val id: String) : MapMode
    @Serializable data class EditHistory(val selectedEditKey: EditKey?) : MapMode
    @Serializable data object Free : MapMode
}
