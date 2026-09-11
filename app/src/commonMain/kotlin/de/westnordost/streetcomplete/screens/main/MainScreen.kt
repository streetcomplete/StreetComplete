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
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import de.westnordost.streetcomplete.data.location.Location
import de.westnordost.streetcomplete.data.messages.Message
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MainBottomSheet
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.controls.MainScreenControls
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
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapCameraState
import de.westnordost.streetcomplete.screens.main.map.rememberMainMapTrackState
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.screens.main.map.toStreetCompleteBoundingBox
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
import org.jetbrains.compose.resources.StringResource
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
import org.maplibre.compose.style.BaseStyle
import org.maplibre.spatialk.units.Bearing
import org.maplibre.spatialk.units.International
import org.maplibre.spatialk.units.extensions.degrees
import org.maplibre.spatialk.units.extensions.inDegrees
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid

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
    val formStateHolder = rememberSaveableStateHolder()
    var sheetSelection by rememberSaveable(stateSaver = MainBottomSheetSelection.Saver) {
        mutableStateOf<MainBottomSheetSelection?>(null)
    }
    var sheetId by rememberSaveable { mutableStateOf("") }
    var focusSheet by rememberSaveable { mutableStateOf(false) }
    var showEditHistorySidebar by rememberSaveable { mutableStateOf(false) }
    var selectedEditKey by rememberSerializable { mutableStateOf<EditKey?>(null) }
    var focusEdit by rememberSaveable { mutableStateOf(false) }
    var displayedLocation by rememberSerializable { mutableStateOf<LocationMeasurement?>(null) }
    var location by remember {
        mutableStateOf(displayedLocation?.let {
            Location(it.position.toLatLon(), it.horizontalAccuracy?.toFloat(International.Meters) ?: 0f, Duration.ZERO)
        })
    }
    var heading by remember { mutableStateOf<HeadingMeasurement?>(null) }
    var locationState by remember { mutableStateOf<LocationState?>(LocationState.ENABLED) }
    var userHasMovedCamera by rememberSaveable { mutableStateOf(false) }
    var formMarkers by remember { mutableStateOf<List<Marker>?>(null) }
    var lastMapClick by remember { mutableStateOf<MapClick?>(null) }
    var lastLongPress by remember { mutableStateOf<Pair<DpOffset, LatLon>?>(null) }
    var showMapContextMenu by remember { mutableStateOf(false) }
    var lastQuestSolved by remember { mutableStateOf<QuestSolvedEvent?>(null) }
    var mapOrigin by remember { mutableStateOf(Offset.Zero) }
    var locationDialog by remember { mutableStateOf<LocationDialog?>(null) }
    val tracks = rememberMainMapTrackState()
    val editItems by editHistoryViewModel.editItems.collectAsState()
    val selectedEdit = if (showEditHistorySidebar) editItems?.find { it.edit.key == selectedEditKey }?.edit else null
    val hasEdits = !editItems.isNullOrEmpty()
    val downloadedTiles by mapViewModel.downloadedTiles.collectAsState()
    val geoUri by viewModel.geoUri.collectAsState()
    val mapAppLauncher = rememberMapAppLauncher()

    fun showSheet(selection: MainBottomSheetSelection) {
        formStateHolder.removeState(sheetId)
        sheetId = Uuid.random().toString()
        sheetSelection = selection
        focusSheet = true
        formMarkers = null
        lastMapClick = null
    }

    fun closeSheet() {
        sheetSelection = null
        formMarkers = null
        lastMapClick = null
        formStateHolder.removeState(sheetId)
    }

    val sheetState = key(sheetSelection) {
        produceState<ShownBottomSheet?>(null, mainBottomSheetViewModel) {
            val selection = sheetSelection ?: return@produceState
            mainBottomSheetViewModel.bottomSheet(selection).collect { sheet ->
                if (selection is MainBottomSheetSelection.Overlay && sheet is ShownBottomSheet.OsmNoteQuest) {
                    // Save the blocking note's key so hiding or deleting it closes its form.
                    sheetSelection = MainBottomSheetSelection.Quest(sheet.quest.key)
                    return@collect
                }
                value = sheet
                // A null emission means the selected object no longer exists, not loading.
                if (sheet == null) closeSheet()
            }
        }
    }
    val shownBottomSheet by sheetState
    val currentSelectedEdit by rememberUpdatedState(selectedEdit)
    val highlightedGeometry by produceState<ElementGeometry?>(null, selectedEdit, editHistoryViewModel) {
        value = selectedEdit?.let { editHistoryViewModel.getEditGeometry(it) }
    }
    val highlightedMarkers by key(shownBottomSheet) {
        produceState<List<Marker>>(emptyList(), mainBottomSheetViewModel) {
            value = shownBottomSheet?.let { mainBottomSheetViewModel.getHighlightedMarkers(it) }.orEmpty()
        }
    }
    val markers = formMarkers ?: highlightedMarkers

    val initialCamera = remember(viewModel) { viewModel.initialCamera }
    val mapState = rememberMapState(runtime, BaseStyle.Json(BASE_STYLE), initialCameraPosition = initialCamera) {
        val state = checkNotNull(LocalMapState.current)
        val showPinsAtZoom by remember(state) { derivedStateOf { state.cameraPosition.zoom >= 13 } }
        val showOverlayAtZoom by remember(state) { derivedStateOf { state.cameraPosition.zoom >= 14 } }
        val showOverlay = selectedOverlay != null && shownBottomSheet !is ShownBottomSheet.OsmQuest &&
            shownBottomSheet !is ShownBottomSheet.OsmNoteQuest && !showEditHistorySidebar
        val questPins = if (!showEditHistorySidebar && sheetSelection == null && showPinsAtZoom) {
            mapViewModel.questPins.collectAsState().value
        } else emptyList()
        val historyPins = if (showEditHistorySidebar && showPinsAtZoom) {
            mapViewModel.editHistoryPins.collectAsState().value
        } else emptyList()
        val styledElements = if (showOverlay && showOverlayAtZoom) {
            mapViewModel.styleableElements.collectAsState().value
        } else emptyList()
        MainMapContent(
            location = location,
            rotation = heading?.let { (it.bearing - (Bearing.North + state.cameraPosition.bearing.degrees)).inDegrees.toFloat() },
            isRecording = tracks.isRecording,
            trackpoints = tracks.currentTrack.map { it.position },
            oldTrackpointsLists = tracks.previousTracks.map { track -> track.map { it.position } },
            shownBottomSheet = shownBottomSheet,
            shownMarkers = markers,
            isShowingUndoHistorySidebar = showEditHistorySidebar,
            selectedOverlay = selectedOverlay,
            selectedEdit = selectedEdit,
            highlightedGeometry = highlightedGeometry,
            downloadedTiles = downloadedTiles,
            questPins = questPins,
            editHistoryPins = historyPins,
            styledElements = styledElements,
            onClickQuest = { properties ->
                val key = mapViewModel.getQuestKey(properties)
                if (key == null || sheetSelection != null) ClickResult.Pass else {
                    showSheet(MainBottomSheetSelection.Quest(key))
                    ClickResult.Consume
                }
            },
            onClickElement = { properties ->
                val key = mapViewModel.getElementKey(properties)
                val overlay = selectedOverlay
                if (key == null || overlay == null || sheetSelection != null) ClickResult.Pass else {
                    showSheet(MainBottomSheetSelection.Overlay(overlay.name, key))
                    ClickResult.Consume
                }
            },
            onClickEdit = { properties ->
                val key = mapViewModel.getEditKey(properties)
                if (key == null) ClickResult.Pass else {
                    selectedEditKey = key
                    focusEdit = true
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
    val cameraPadding = if (sheetSelection != null) sheetPadding else PaddingValues(0.dp)
    val isNavigationMode = cameraState.isNavigationMode
    val isFollowingPosition = cameraState.isFollowingPosition
    val isRecordingTracks = tracks.isRecording

    fun getOffset(position: LatLon): Offset? = mapState.screenLocationFromPosition(position.toPosition())?.let {
        with(density) { Offset(it.x.toPx(), it.y.toPx()) } + mapOrigin
    }
    val displayedPosition = remember(displayedLocation, viewport, mapCamera, mapOrigin) {
        displayedLocation?.position?.toLatLon()?.let(::getOffset)
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
        showSheet(MainBottomSheetSelection.CreateNote(position, trackpoints))
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
                        location = fix
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
                        location = null
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
    // Freeze follow/navigation before moving the camera for a form or history selection.
    LaunchedEffect(mapState, sheetSelection, showEditHistorySidebar, selectedEditKey) {
        val selection = sheetSelection
        if (selection != null) {
            cameraState.freeze()
            if (focusSheet) {
                val sheet = snapshotFlow { sheetState.value }.filterNotNull().first()
                focusSheet = false
                when (selection) {
                    is MainBottomSheetSelection.CreateNote -> mapState.animateCameraPosition(
                        mapState.cameraPosition.copy(target = selection.position.toPosition()), 300.milliseconds,
                    )
                    else -> when (sheet) {
                        is ShownBottomSheet.OsmQuest -> cameraState.focus(sheet.quest.geometry)
                        is ShownBottomSheet.OsmNoteQuest -> cameraState.focus(sheet.quest.geometry)
                        else -> Unit
                    }
                }
            }
        } else if (showEditHistorySidebar) {
            cameraState.freeze()
            if (selectedEditKey == null) cameraState.clearFocus()
            else if (focusEdit) {
                val edit = snapshotFlow { currentSelectedEdit }.filterNotNull().first()
                focusEdit = false
                cameraState.focus(editHistoryViewModel.getEditGeometry(edit))
            }
        } else {
            cameraState.unfreeze(displayedLocation?.position?.toLatLon(), getTrackBearing(tracks.currentTrack))
            cameraState.endFocus()
        }
    }
    LaunchedEffect(editItems) {
        val items = editItems ?: return@LaunchedEffect
        if (items.none { it.edit.key == selectedEditKey }) selectedEditKey = null
        if (items.isEmpty() && showEditHistorySidebar) {
            cameraState.clearFocus()
            showEditHistorySidebar = false
        }
    }
    LaunchedEffect(selectedOverlay) {
        val selection = sheetSelection as? MainBottomSheetSelection.Overlay
        if (selection != null && selection.name != selectedOverlay?.name) closeSheet()
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
                if (sheetSelection != null && position != null) {
                    // Name suggestions use a small tap tolerance; rendered feature queries remain point-only.
                    lastMapClick = MapClick(position, metersPerDp * 14)
                } else if (showEditHistorySidebar) {
                    cameraState.clearFocus()
                    showEditHistorySidebar = false
                    selectedEditKey = null
                }
                ClickResult.Consume
            },
            onMapLongClick = { event ->
                val position = event.position?.toLatLon()
                if (sheetSelection == null && !showEditHistorySidebar && position != null) {
                    lastLongPress = event.screenOffset to position
                    showMapContextMenu = true
                }
                ClickResult.Consume
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
                    if (sheetSelection is MainBottomSheetSelection.Overlay) closeSheet()
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
                displayedLocationOffset = displayedPosition,
                onClickLocation = ::clickLocation,
                onClickLocationPointer = ::followLocation,

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
                            showSheet(MainBottomSheetSelection.Overlay(overlay.name))
                            position?.let { mapState.setCameraPosition(mapState.cameraPosition.copy(target = it)) }
                        }
                    } else {
                        showToast = Toast.DownloadAreaTooBig
                    }
                },

                hasEdits = hasEdits,
                isUndoEnabled = !isUploadingOrDownloading,
                onClickUndo = {
                    selectedEditKey = editItems?.lastOrNull()?.edit?.key
                    focusEdit = true
                    showEditHistorySidebar = true
                },

                metersPerDp = metersPerDp,
                userHasMovedMap = userHasMovedCamera,
            )
        }

        val dir = LocalLayoutDirection.current.dir
        AnimatedVisibility(
            visible = showEditHistorySidebar && hasEdits,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { -it * dir }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { -it * dir }),
        ) {
            EditHistorySidebar(
                editItems = editItems.orEmpty(),
                selectedEdit = selectedEdit,
                onSelectEdit = {
                    selectedEditKey = it.key
                    focusEdit = true
                },
                onUndoEdit = { editHistoryViewModel.undo(it.key) },
                onDismissRequest = {
                    cameraState.clearFocus()
                    showEditHistorySidebar = false
                    selectedEditKey = null
                },
                getEditElement = editHistoryViewModel::getEditElement,
            )
        }

        AnimatedContent(
            targetState = shownBottomSheet?.let { sheetId to it },
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
                formStateHolder.SaveableStateProvider(id) {
                    MainBottomSheet(
                        onDismiss = ::closeSheet,
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
                        onSetMapMarkers = { if (id == sheetId) formMarkers = it.toList() },
                        getOffset = ::getOffset,
                        lastMapClick = lastMapClick,
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
