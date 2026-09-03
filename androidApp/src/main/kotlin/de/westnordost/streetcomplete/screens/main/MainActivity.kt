package de.westnordost.streetcomplete.screens.main

import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.FeedsUpdater
import de.westnordost.streetcomplete.data.PeriodicCleaner
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.LazyMapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.key
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.AutoSyncer
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.about.AboutActivity
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.icon
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.MapFragment
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.Padding
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPadding
import de.westnordost.streetcomplete.screens.settings.SettingsActivity
import de.westnordost.streetcomplete.screens.user.UserActivity
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.toDpOffset
import de.westnordost.streetcomplete.ui.theme.AppTheme
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toOffset
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.view.toAndroidResourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.maplibre.compose.location.LocationEvent
import org.maplibre.compose.location.LocationPermission
import org.maplibre.compose.location.LocationProvider
import org.maplibre.compose.location.LocationRequest
import org.maplibre.compose.location.LocationUnavailableReason
import org.maplibre.compose.location.SystemSettingsLauncher
import kotlin.math.PI
import kotlin.math.sqrt

/** Controls the main view.
 *
 *  The logical sub components of this main view are all outsourced into individual child fragments
 *  with which this fragment communicates with.
 *
 *  The child fragments do not communicate with each other but only with their parent (this class)
 *  and the parent then controls its children. Hence, all the logic when interacting with the
 *  map / bottom sheets / sidebars / buttons etc. passes through this class and this is why this
 *  class implements all the listeners of its child fragments.
 *
 *  This class does not contain so much logic itself, it delegates most of it to its children.
 *  Think of it as the wiring that binds all the components together.
 *
 *  Still, as this is by far the largest in terms of lines of code. For easier reading, in
 *  IntelliJ, you can collapse sections of this class that start with "//region" using the little
 *  [-] icon next to it.
 *
 */
class MainActivity :
    BaseActivity(),
    // listeners to child fragments:
    MapFragment.Listener,
    MainMapFragment.Listener,
    // listeners to changes to data:
    VisibleQuestsSource.Listener,
    MapDataWithEditsSource.Listener,
    // rest
    AndroidScopeComponent {

    override val scope: Scope by activityScope()

    private val autoSyncer: AutoSyncer by inject()
    private val prefs: Preferences by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val notesSource: NotesWithEditsSource by inject()
    private val questsHiddenSource: QuestsHiddenSource by inject()
    private val feedsUpdater: FeedsUpdater by inject()
    private val featureDictionary: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val locationProvider: LocationProvider by inject()
    private val systemSettingsLauncher: SystemSettingsLauncher by inject()
    private val periodicCleaner: PeriodicCleaner by inject()

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()
    private val mainBottomSheetViewModel by viewModel<MainBottomSheetViewModel>()

    private val showMapContextMenu = mutableStateOf(false)
    private val lastMapLongPress = mutableStateOf<Pair<Offset, LatLon>?>(null)

    private val lastMapClick = mutableStateOf<MapClick?>(null)

    private var windowInfo: WindowInfo? = null

    // for freezing the map while sidebar is open
    private var wasFollowingPosition: Boolean? = null
    private var wasNavigationMode: Boolean? = null

    private val mapFragment: MainMapFragment? get() =
        supportFragmentManager.findFragmentByTag(TAG_MAP) as MainMapFragment?

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    //region Lifecycle - Android Lifecycle Callbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val root = RelativeLayout(this)
        val compose = ComposeView(this)
        val mapContainer = FragmentContainerView(this).also { it.id = 1 }
        root.addView(mapContainer, ViewGroup.LayoutParams(-1, -1))
        root.addView(compose, ViewGroup.LayoutParams(-1, -1))

        setContentView(root)

        if (savedInstanceState == null) {
            handleIntent(intent)

            supportFragmentManager.commit {
                add(mapContainer, MainMapFragment(), TAG_MAP)
            }
        }

        lifecycle.addObserver(autoSyncer)

        feedsUpdater.updateAtMostDaily()
        // this must be enqueued once the UI is started, i.e. not in headless mode. This is why
        // it is done here, rather than in AppInitializer. Reason is that
        // AppInitializer.initialize() is also executed when a background job is run. But we don't
        // want to enqueue the cleanup job again while running the cleanup job, but only once after
        // the user actually opened the app!
        periodicCleaner.enqueue()

        compose.setContent { AppTheme {
            val mapAppLauncher = rememberMapAppLauncher()
            var lastQuestSolved by remember { mutableStateOf<QuestSolvedEvent?>(null) }

            windowInfo = LocalWindowInfo.current
            val context = LocalContext.current

            MainScreen(
                viewModel = viewModel,
                editHistoryViewModel = editHistoryViewModel,
                mainBottomSheetViewModel = mainBottomSheetViewModel,
                onClickZoomIn = ::onClickZoomIn,
                onClickZoomOut = ::onClickZoomOut,
                onZoomDrag = ::onZoomDrag,
                onClickCompass = ::onClickCompassButton,
                onClickLocation = ::onClickLocationButton,
                onClickLocationPointer = ::onClickLocationPointer,
                onClickCreate = ::onClickCreateButton,
                onClickStopTrackRecording = ::onClickTracksStop,
                onDownload = ::onClickDownload,
                onClickSettings = {
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                },
                onClickQuestSettings = {
                    context.startActivity(SettingsActivity.createLaunchQuestSettingsIntent(context))
                },
                onClickAbout = {
                    context.startActivity(Intent(context, AboutActivity::class.java))
                },
                onClickProfile = {
                    context.startActivity(Intent(context, UserActivity::class.java))
                },
                onClickLogin = {
                    val intent = Intent(context, UserActivity::class.java)
                    intent.putExtra(UserActivity.EXTRA_LAUNCH_AUTH, true)
                    context.startActivity(intent)
                },
                onSetMapMarkers = { markers ->
                    mapFragment?.setMarkersForCurrentHighlighting(markers)
                },
                onSolvedQuest = { icon, position ->
                    val offset = root.getLocationInWindow()
                    val startPos = mapFragment?.getPointOf(position)!!

                    startPos.x += offset.x
                    startPos.y += offset.y

                    lastQuestSolved = QuestSolvedEvent(icon, startPos.toOffset())
                },
                getOffset = { position ->
                    val offset = root.getLocationInWindow()
                    val position = mapFragment?.getPointOf(position)!!
                    position.x += offset.x
                    position.y += offset.y
                    position.toOffset()
                },
                lastMapClick = lastMapClick.value,
            )

            lastQuestSolved?.let { LastQuestSolvedEffect(it) }

            val lastLongPressOffset = lastMapLongPress.value?.first ?: Offset.Zero
            val lastLongPressPosition = lastMapLongPress.value?.second
            MapContextMenu(
                expanded = showMapContextMenu.value,
                onDismissRequest = { showMapContextMenu.value = false },
                onClickCreateNote = { lastLongPressPosition?.let { onClickCreateNote(it) } },
                onClickCreateTrack = { onClickCreateTrack() },
                isOpenLocationAvailable = mapAppLauncher.isAvailable(),
                onClickOpenLocation = {
                    if (lastLongPressPosition != null) {
                        mapAppLauncher.openAt(
                            position = lastLongPressPosition,
                            zoom = mapFragment?.cameraPosition?.zoom ?: 18.0
                        )
                    }
                },
                offset = lastLongPressOffset.toDpOffset()
            )
        } }

        observe(editHistoryViewModel.selectedEdit) { edit ->
            if (edit != null) {
                val geometry = editHistoryViewModel.getEditGeometry(edit)
                mapFragment?.startFocus(geometry, null)
                mapFragment?.highlightGeometry(geometry)
                mapFragment?.highlightPins(edit.icon!!.toAndroidResourceId()!!, listOf(edit.position))
                mapFragment?.hideOverlay()
            } else if (editHistoryViewModel.isShowingSidebar.value) {
                mapFragment?.clearFocus()
                mapFragment?.clearHighlighting()
                mapFragment?.hideOverlay() // because clearHighlighting shows overlay again :-/
            }
        }
        observe(editHistoryViewModel.isShowingSidebar) { isShowingSidebar ->
            if (!isShowingSidebar) {
                unfreezeMap()
                mapFragment?.clearFocus()
                mapFragment?.clearHighlighting()
                mapFragment?.pinMode = MainMapFragment.PinMode.QUESTS
            } else {
                freezeMap()
                mapFragment?.hideOverlay()
                mapFragment?.pinMode = MainMapFragment.PinMode.EDITS
            }
        }
        observe(viewModel.geoUri) { geoUri ->
            if (geoUri != null) {
                viewModel.consumeGeoUri()
                mapFragment?.setInitialCameraPosition(geoUri)
                viewModel.isFollowingPosition.value = mapFragment?.isFollowingPosition ?: false
                viewModel.isNavigationMode.value = mapFragment?.isNavigationMode ?: false
            }
        }
        observe(mainBottomSheetViewModel.shownBottomSheet) { shownBottomSheet ->
            updateBottomSheetElementPosition()
            if (shownBottomSheet != null) {
                freezeMap()
                when (shownBottomSheet) {
                    is ShownBottomSheet.CreateOsmNote -> {
                        /* nothing more */
                    }
                    is ShownBottomSheet.OsmNoteQuest -> {
                        showQuestDetailsOnMap(shownBottomSheet.quest, null)
                    }
                    is ShownBottomSheet.OsmQuest -> {
                        val element = shownBottomSheet.element
                        showQuestDetailsOnMap(shownBottomSheet.quest, element)
                    }
                    is ShownBottomSheet.Overlay -> {
                        val element = shownBottomSheet.element
                        if (element != null) {
                            showOverlayElementDetailsOnMap(
                                overlay = shownBottomSheet.overlay,
                                element = element,
                                geometry = shownBottomSheet.geometry!!
                            )
                        } else {
                            showOverlayForNewElementOnMap(shownBottomSheet.overlay)
                        }
                    }
                }
            } else {
                clearHighlighting()
                unfreezeMap()
                mapFragment?.endFocus()
            }
        }
        observe(viewModel.selectedOverlay) { selectedOverlay ->
            if (mainBottomSheetViewModel.shownBottomSheet.value is ShownBottomSheet.Overlay) {
                mainBottomSheetViewModel.closeBottomSheet()
            }
        }
        observe(locationProvider.updates(LocationRequest())) { locationEvent ->
            viewModel.locationState.value = when (locationEvent) {
                is LocationEvent.Fix -> LocationState.UPDATING
                is LocationEvent.Unavailable -> when (locationEvent.reason) {
                    LocationUnavailableReason.ServicesDisabled -> LocationState.ALLOWED
                    LocationUnavailableReason.TemporarilyUnavailable -> LocationState.SEARCHING
                    LocationUnavailableReason.PermissionDenied -> LocationState.DENIED
                    LocationUnavailableReason.Unsupported,
                    LocationUnavailableReason.Misconfigured,
                    LocationUnavailableReason.UnexpectedFailure -> null
                }
            }
            mapFragment?.onLocationEvent(locationEvent)
        }
    }

    override fun onStart() {
        super.onStart()

        updateScreenOn()

        visibleQuestsSource.addListener(this)
        mapDataWithEditsSource.addListener(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        val data = intent.data?.toString() ?: return
        viewModel.setUri(data)
    }

    override fun onStop() {
        super.onStop()

        visibleQuestsSource.removeListener(this)
        mapDataWithEditsSource.removeListener(this)
    }

    //endregion

    /* ------------------------------- Preferences listeners ------------------------------------ */

    private fun updateScreenOn() {
        if (prefs.keepScreenOn) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    //region QuestsMapFragment - Callbacks from the map with its quest pins

    /* ---------------------------------- MapFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        viewModel.geoUri.value?.let { geoUri ->
            viewModel.consumeGeoUri()
            mapFragment?.setInitialCameraPosition(geoUri)
        }
        viewModel.isFollowingPosition.value = mapFragment?.isFollowingPosition ?: false
        viewModel.isNavigationMode.value = mapFragment?.isNavigationMode ?: false
        viewModel.isRecordingTracks.value = mapFragment?.isRecordingTracks ?: false
        viewModel.mapCamera.value = mapFragment?.cameraPosition
        viewModel.metersPerDp.value = mapFragment?.getMetersPerPixel() ?: 0.0
        updateBottomSheetElementPosition()
        updateDisplayedPosition()
    }

    override fun onMapIsChanging(camera: CameraPosition) {
        viewModel.mapCamera.value = camera
        viewModel.metersPerDp.value = mapFragment?.getMetersPerPixel() ?: 0.0
        updateBottomSheetElementPosition()
        updateDisplayedPosition()
    }

    override fun onPanBegin() {
        /* panning only results in not following location anymore if a location is already known
           and displayed
         */
        if (mapFragment?.displayedLocation != null) {
            setIsFollowingPosition(false)
        }
    }

    override fun onUserCameraMoveStarted() {
        viewModel.userHasMovedCamera.value = true
    }

    override fun onLongPress(point: PointF, position: LatLon) {
        if (mainBottomSheetViewModel.shownBottomSheet.value != null || editHistoryViewModel.isShowingSidebar.value) return

        lastMapLongPress.value = Pair(Offset(point.x, point.y), position)
        showMapContextMenu.value = true
    }

    /* ---------------------------- MainMapFragment.Listener --------------------------- */

    override fun onClickedQuest(questKey: QuestKey) {
        if (mainBottomSheetViewModel.shownBottomSheet.value != null) return
        mainBottomSheetViewModel.showQuest(questKey)
    }

    override fun onClickedEdit(editKey: EditKey) {
        editHistoryViewModel.select(editKey)
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        if (mainBottomSheetViewModel.shownBottomSheet.value != null) {
            lastMapClick.value = MapClick(position, clickAreaSizeInMeters)
        } else if (editHistoryViewModel.isShowingSidebar.value) {
            editHistoryViewModel.hideSidebar()
        }
    }

    override fun onClickedElement(elementKey: ElementKey) {
        val overlay = viewModel.selectedOverlay.value ?: return
        if (mainBottomSheetViewModel.shownBottomSheet.value != null) return
        mainBottomSheetViewModel.showElementInOverlay(overlay, elementKey)
    }

    override fun onDisplayedLocationDidChange() {
        updateDisplayedPosition()
    }

    private fun updateDisplayedPosition() {
        viewModel.displayedPosition.value = getDisplayedPoint()?.toOffset()
    }

    private fun updateBottomSheetElementPosition() {
        val bottomSheetElementPosition = mainBottomSheetViewModel.shownBottomSheet.value?.position
        mainBottomSheetViewModel.geometryOffsetInWindow.value =
            if (bottomSheetElementPosition != null) mapFragment?.getPointOf(bottomSheetElementPosition)?.toOffset()
            else null
    }

    private fun getDisplayedPoint(): PointF? {
        val mapFragment = mapFragment ?: return null
        val displayedPosition = mapFragment.displayedLocation?.position?.value?.toLatLon() ?: return null
        return mapFragment.getPointOf(displayedPosition)
    }

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread
    override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val questKey =
            when (val shown = mainBottomSheetViewModel.shownBottomSheet.value) {
                is ShownBottomSheet.OsmNoteQuest -> shown.quest.key
                is ShownBottomSheet.OsmQuest -> shown.quest.key
                else -> return
        }
        // open quest has been deleted
        if (questKey in removed) {
            mainBottomSheetViewModel.closeBottomSheet()
        }
    }

    @AnyThread
    override fun onInvalidated() {
        val questKey =
            when (val shown = mainBottomSheetViewModel.shownBottomSheet.value) {
                is ShownBottomSheet.OsmNoteQuest -> shown.quest.key
                is ShownBottomSheet.OsmQuest -> shown.quest.key
                else -> return
            }

        lifecycleScope.launch {
            val openQuest = withContext(Dispatchers.IO) { visibleQuestsSource.get(questKey) }
            // open quest does not exist anymore after visible quest invalidation
            if (openQuest == null) {
                mainBottomSheetViewModel.closeBottomSheet()
            }
        }
    }

    /* ---------------------------- MapDataWithEditsSource.Listener ----------------------------- */

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val elementKey = (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)?.element?.key ?: return
        if (elementKey in deleted) {
            mainBottomSheetViewModel.closeBottomSheet()
        }
    }

    @AnyThread
    override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        val elementKey = (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)?.element?.key ?: return
        lifecycleScope.launch {
            val openElement = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) }
            // open element does not exist anymore after download
            if (openElement == null) {
                mainBottomSheetViewModel.closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onCleared() {
        val elementKey = (mainBottomSheetViewModel.shownBottomSheet.value as? ShownBottomSheet.Overlay)?.element?.key ?: return
        mainBottomSheetViewModel.closeBottomSheet()
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++ VIEW CONTROL ++++++++++++++++++++++++++++++++++++++ */

    //region Buttons - Functionality for the buttons in the main view

    private fun onClickDownload() {
        val downloadBbox = getDownloadArea() ?: return
        viewModel.download(downloadBbox)
    }

    private fun onClickZoomOut() {
        mapFragment?.updateCameraPosition(300) { zoomBy = -1.0 }
    }

    private fun onClickZoomIn() {
        mapFragment?.updateCameraPosition(300) { zoomBy = +1.0 }
    }

    private fun onZoomDrag(dp: Float) {
        mapFragment?.updateCameraPosition(300) { zoomBy = dp / 20.0 }
    }

    private fun onClickTracksStop() {
        // hide the track information
        viewModel.isRecordingTracks.value = false
        val mapFragment = mapFragment ?: return
        mapFragment.stopPositionTrackRecording()
        val pos = mapFragment.displayedLocation?.position?.value?.toLatLon() ?: return
        composeNote(pos, mapFragment.recordedTracks.takeIf { it.isNotEmpty() })
    }

    private fun onClickCompassButton() {
        // Clicking the compass button will always rotate the map back to north and remove tilt
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition ?: return

        // if the user wants to rotate back north, it means he also doesn't want to use nav mode anymore
        if (mapFragment.isNavigationMode) {
            mapFragment.updateCameraPosition(300) { rotation = 0.0 }
            setIsNavigationMode(false)
        } else {
            mapFragment.updateCameraPosition(300) {
                rotation = 0.0
                tilt = 0.0
            }
        }
    }

    private fun onClickLocationButton() {
        val mapFragment = mapFragment ?: return

        val permission = locationProvider.permission.value
        if (permission is LocationPermission.NotGranted) {
            if (permission.canRequest != false) {
                if (!permission.shouldShowRationale) {
                    locationProvider.requestPermission()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle(R.string.no_location_permission_warning_title)
                        .setMessage(R.string.no_location_permission_warning)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            locationProvider.requestPermission()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                }
            } else {
                if (systemSettingsLauncher.canOpenApplicationSettings) {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.turn_on_location_request)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            systemSettingsLauncher.openApplicationSettings()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
                } else {
                    toast(R.string.no_gps_no_quests)
                }
            }
        } else {
            when {
                viewModel.locationState.value == LocationState.ALLOWED -> {
                    if (systemSettingsLauncher.canOpenLocationServicesSettings) {
                        AlertDialog.Builder(this)
                            .setMessage(R.string.turn_on_location_request)
                            .setPositiveButton(R.string.ok) { _, _ ->
                                systemSettingsLauncher.openLocationServicesSettings()
                            }
                            .setNegativeButton(android.R.string.cancel, null)
                            .show()
                    } else {
                        toast(R.string.no_gps_no_quests)
                    }
                }
                !mapFragment.isFollowingPosition -> {
                    setIsFollowingPosition(true)
                }
                else -> {
                    setIsNavigationMode(!mapFragment.isNavigationMode)
                }
            }
        }
    }

    private fun onClickLocationPointer() {
        setIsFollowingPosition(true)
    }

    private fun onClickCreateButton() {
        val overlay = viewModel.selectedOverlay.value ?: return
        mainBottomSheetViewModel.showCreateElementInOverlay(overlay)
    }

    private fun setIsNavigationMode(navigation: Boolean) {
        mapFragment?.isNavigationMode = navigation
        viewModel.isNavigationMode.value = navigation
    }

    private fun setIsFollowingPosition(follow: Boolean) {
        mapFragment?.isFollowingPosition = follow
        viewModel.isFollowingPosition.value = follow
        if (follow) mapFragment?.centerCurrentPositionIfFollowing()
    }

    private fun getDownloadArea(): BoundingBox? {
        val displayArea = mapFragment?.getDisplayedArea()
        if (displayArea == null) {
            toast(R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG)
            return null
        }

        val enclosingBBox = displayArea.asBoundingBoxOfEnclosingTiles(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        val areaInSqKm = enclosingBBox.area() / 1000000
        if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
            toast(R.string.download_area_too_big, Toast.LENGTH_LONG)
            return null
        }

        // below a certain threshold, it does not make sense to download, so let's enlarge it
        if (areaInSqKm < ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM) {
            val cameraPosition = mapFragment?.cameraPosition
            if (cameraPosition != null) {
                val radius = sqrt(1000000 * ApplicationConstants.MIN_DOWNLOADABLE_AREA_IN_SQKM / PI)
                return cameraPosition.position.enclosingBoundingBox(radius)
            }
        }

        return enclosingBBox
    }

    /* -------------------------------------- Context Menu -------------------------------------- */

    private fun onClickCreateNote(pos: LatLon) {
        if ((mapFragment?.cameraPosition?.zoom ?: 0.0) < ApplicationConstants.NOTE_MIN_ZOOM) {
            toast(R.string.create_new_note_unprecise)
            return
        }

        composeNote(pos)
    }

    private fun composeNote(pos: LatLon, trackpoints: List<Trackpoint>? = null) {
        mainBottomSheetViewModel.showCreateNote(trackpoints)

        mapFragment?.updateCameraPosition(300) {
            position = pos
            padding = getOpenQuestFormMapPadding()
        }
    }

    private fun onClickCreateTrack() {
        mapFragment?.startPositionTrackRecording()
        viewModel.isRecordingTracks.value = true
    }

    //endregion

    //region Bottom Sheet - Controlling the bottom sheet and its interaction with the map

    /** Open or replace the bottom sheet. */
    private fun showBottomSheet(content: ShownBottomSheet) {
        freezeMap()
    }

    /** Make the map not follow the user's location anymore temporarily */
    private fun freezeMap() {
        val mapFragment = mapFragment ?: return
        if (wasFollowingPosition == null) wasFollowingPosition = mapFragment.isFollowingPosition
        if (wasNavigationMode == null) wasNavigationMode = mapFragment.isNavigationMode
        mapFragment.isFollowingPosition = false
        mapFragment.isNavigationMode = false
    }

    /** Make the map follow the user's location again (if it was following before) */
    private fun unfreezeMap() {
        wasFollowingPosition?.let { mapFragment?.isFollowingPosition = it }
        wasNavigationMode?.let { mapFragment?.isNavigationMode = it }
        wasFollowingPosition = null
        wasNavigationMode = null
    }

    private fun clearHighlighting() {
        mapFragment?.clearHighlighting()
    }

    //endregion

    //region Bottom sheets

    @UiThread
    private fun showOverlayForNewElementOnMap(overlay: Overlay) {
        val mapFragment = mapFragment ?: return

        mapFragment.updateCameraPosition {
            position = getCrosshairOffset()?.toPointF()?.let { mapFragment.getPositionAt(it) }
            padding = getOpenQuestFormMapPadding()
        }
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private suspend fun showOverlayElementDetailsOnMap(overlay: Overlay, element: Element, geometry: ElementGeometry) {
        val mapFragment = mapFragment ?: return

        mapFragment.updateCameraPosition {
            padding = getOpenQuestFormMapPadding()
        }

        mapFragment.highlightGeometry(geometry)
        mapFragment.highlightPins(overlay.icon.toAndroidResourceId()!!, listOf(geometry.center))
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private fun showQuestDetailsOnMap(quest: Quest, element: Element?) {
        val mapFragment = mapFragment ?: return

        if (quest is OsmQuest && element != null) {
            showHighlightedElements(quest, element)
        }
        mapFragment.startFocus(quest.geometry, getOpenQuestFormMapPadding())
        mapFragment.highlightGeometry(quest.geometry)
        mapFragment.highlightPins(quest.type.icon.toAndroidResourceId()!!, quest.markerLocations)
        mapFragment.hideNonHighlightedPins(quest.key)
        mapFragment.hideOverlay()
    }

    private fun showHighlightedElements(quest: OsmQuest, element: Element) {
        val bbox = quest.geometry.bounds.enlargedBy(quest.type.highlightedElementsRadius)
        val lazyMapData = LazyMapDataWithGeometry(bbox, mapDataWithEditsSource)

        val levels = parseLevelsOrNull(element.tags)

        lifecycleScope.launch(Dispatchers.Default) {
            val elements = withContext(Dispatchers.IO) {
                quest.type.getHighlightedElements(element, lazyMapData)
            }
            val markers = elements.mapNotNull { e ->
                // don't highlight "this" element
                if (element == e) return@mapNotNull null
                // include only elements with the same (=intersecting) level, if any
                val eLevels = parseLevelsOrNull(e.tags)
                if (!levels.levelsIntersect(eLevels)) return@mapNotNull null
                // include only elements with the same layer, if any
                if (element.tags["layer"] != e.tags["layer"]) return@mapNotNull null

                val geometry = lazyMapData.getGeometry(e.type, e.id) ?: return@mapNotNull null
                val icon = getIcon(featureDictionary.value, e)
                val title = getTitle(e.tags)
                Marker(geometry, icon, title)
            }.toList()

            withContext(Dispatchers.Main) { mapFragment?.setMarkersForCurrentHighlighting(markers) }
        }
    }

    private fun getCrosshairOffset(): Offset? {
        val windowInfo = windowInfo ?: return null
        val padding = getOpenQuestFormMapPadding() ?: return null
        val size = windowInfo.containerSize
        return Offset(
            (padding.left + (size.width - padding.left - padding.right) / 2).toFloat(),
            (padding.top + (size.height - padding.top - padding.bottom) / 2).toFloat()
        )
    }

    private fun Offset.toPointF() = PointF(x, y)

    private fun getOpenQuestFormMapPadding(): Padding? {
        val windowInfo = windowInfo ?: return null
        val layoutDirection = if (resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        val density = Density(this)
        return Dimensions.getOpenQuestFormMapPadding(windowInfo).toPadding(layoutDirection, density)
    }

    //endregion

    companion object {
        private const val TAG_MAP = "MainMapFragment"
    }
}
