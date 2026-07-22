package de.westnordost.streetcomplete.screens.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.graphics.Insets
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.FeedsUpdater
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
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.Overlay
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.OsmNoteQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.databinding.ActivityMainBinding
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.screens.BaseActivity
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
import de.westnordost.streetcomplete.ui.common.quest.MapClick
import de.westnordost.streetcomplete.ui.common.quest.Marker
import de.westnordost.streetcomplete.ui.ktx.toDpOffset
import de.westnordost.streetcomplete.ui.theme.Dimensions
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.truncateTo6Decimals
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.view.toAndroidResourceId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityScope
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
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

    private val questAutoSyncer: QuestAutoSyncer by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val prefs: Preferences by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val notesSource: NotesWithEditsSource by inject()
    private val questsHiddenSource: QuestsHiddenSource by inject()
    private val feedsUpdater: FeedsUpdater by inject()
    private val featureDictionary: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val mapAppLauncher: MapAppLauncher by inject()

    private lateinit var locationManager: FineLocationManager

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()

    private val showMapContextMenu = mutableStateOf(false)
    private val lastMapLongPress = mutableStateOf<Pair<Offset, LatLon>?>(null)
    private val lastQuestSolved = mutableStateOf<QuestSolvedEvent?>(null)

    private val shownBottomSheet = mutableStateOf<BottomSheetContent?>(null)
    private val lastMapClick = mutableStateOf<MapClick?>(null)

    private var windowInfo: WindowInfo? = null

    private lateinit var binding: ActivityMainBinding

    // for freezing the map while sidebar is open
    private var wasFollowingPosition: Boolean? = null
    private var wasNavigationMode: Boolean? = null

    private val mapFragment: MainMapFragment? get() =
        supportFragmentManager.findFragmentById(R.id.mapFragment) as MainMapFragment?

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    private val requestLocationPermissionResultReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.getBooleanExtra(LocationRequestFragment.GRANTED, false)) {
                toast(R.string.no_gps_no_quests, Toast.LENGTH_LONG)
            }
        }
    }

    //region Lifecycle - Android Lifecycle Callbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            handleIntent(intent)
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            requestLocationPermissionResultReceiver,
            IntentFilter(LocationRequestFragment.REQUEST_LOCATION_PERMISSION_RESULT)
        )
        supportFragmentManager.commit { add(LocationRequestFragment(), TAG_LOCATION_REQUEST) }

        lifecycle.addObserver(questAutoSyncer)
        feedsUpdater.updateAtMostDaily()

        locationManager = FineLocationManager(this, this::onLocationChanged)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.controls.content {
            val isMapAppLaunchAvailable = remember { mapAppLauncher.isAvailable() }

            windowInfo = LocalWindowInfo.current

            // color for HUD elements without a background (e.g. scalebar, attribution button)
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colors.onSurface
            ) {
                MainScreen(
                    viewModel = viewModel,
                    editHistoryViewModel = editHistoryViewModel,
                    onClickZoomIn = ::onClickZoomIn,
                    onClickZoomOut = ::onClickZoomOut,
                    onZoomDrag = ::onZoomDrag,
                    onClickCompass = ::onClickCompassButton,
                    onClickLocation = ::onClickLocationButton,
                    onClickLocationPointer = ::onClickLocationPointer,
                    onClickCreate = ::onClickCreateButton,
                    onClickStopTrackRecording = ::onClickTracksStop,
                    onClickDownload = ::onClickDownload,
                    onExplainedNeedForLocationPermission = ::requestLocation
                )
            }

            lastQuestSolved.value?.let {
                LastQuestSolvedEffect(it)
            }

            val lastLongPressOffset = lastMapLongPress.value?.first ?: Offset.Zero
            val lastLongPressPosition = lastMapLongPress.value?.second
            MapContextMenu(
                expanded = showMapContextMenu.value,
                onDismissRequest = { showMapContextMenu.value = false },
                onClickCreateNote = { lastLongPressPosition?.let { onClickCreateNote(it) } },
                onClickCreateTrack = { onClickCreateTrack() },
                isOpenLocationAvailable = isMapAppLaunchAvailable,
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
        }

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
    }

    override fun onStart() {
        super.onStart()

        updateScreenOn()

        visibleQuestsSource.addListener(this)
        mapDataWithEditsSource.addListener(this)
        locationAvailabilityReceiver.addListener(::updateLocationAvailability)

        updateLocationAvailability(isLocationAvailable)
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

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        findViewById<View>(R.id.main).requestLayout()
    }

    override fun onStop() {
        super.onStop()

        visibleQuestsSource.removeListener(this)
        mapDataWithEditsSource.removeListener(this)
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)

        locationManager.removeUpdates()
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
        updateDisplayedPosition()
    }

    override fun onMapIsChanging(camera: CameraPosition) {
        viewModel.mapCamera.value = camera
        viewModel.metersPerDp.value = mapFragment?.getMetersPerPixel() ?: 0.0
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
        if (shownBottomSheet.value != null || editHistoryViewModel.isShowingSidebar.value) return

        lastMapLongPress.value = Pair(Offset(point.x, point.y), position)
        showMapContextMenu.value = true
    }

    /* ---------------------------- MainMapFragment.Listener --------------------------- */

    override fun onClickedQuest(questKey: QuestKey) {
        showInBottomSheet(BottomSheetContent.Quest(questKey))
    }

    override fun onClickedEdit(editKey: EditKey) {
        editHistoryViewModel.select(editKey)
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        if (shownBottomSheet.value != null) {
            lastMapClick.value = MapClick(position, clickAreaSizeInMeters)
        } else if (editHistoryViewModel.isShowingSidebar.value) {
            editHistoryViewModel.hideSidebar()
        }
    }

    override fun onClickedElement(elementKey: ElementKey) {
        showInBottomSheet(BottomSheetContent.Overlay(elementKey))
    }

    override fun onDisplayedLocationDidChange() {
        updateDisplayedPosition()
    }

    private fun updateDisplayedPosition() {
        viewModel.displayedPosition.value = getDisplayedPoint()?.let { Offset(it.x, it.y) }
    }

    private fun getDisplayedPoint(): PointF? {
        val mapFragment = mapFragment ?: return null
        val displayedPosition = mapFragment.displayedLocation?.toLatLon() ?: return null
        return mapFragment.getPointOf(displayedPosition)
    }

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread
    override fun onUpdated(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val questKey = (shownBottomSheet.value as? BottomSheetContent.Quest)?.questKey ?: return
        // open quest has been deleted
        if (questKey in removed) {
            lifecycleScope.launch { closeBottomSheet() }
        }
    }

    @AnyThread
    override fun onInvalidated() {
        val questKey = (shownBottomSheet.value as? BottomSheetContent.Quest)?.questKey ?: return
        lifecycleScope.launch {
            val openQuest = withContext(Dispatchers.IO) { visibleQuestsSource.get(questKey) }
            // open quest does not exist anymore after visible quest invalidation
            if (openQuest == null) {
                closeBottomSheet()
            }
        }
    }

    /* ---------------------------- MapDataWithEditsSource.Listener ----------------------------- */

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val elementKey = (shownBottomSheet.value as? BottomSheetContent.Overlay)?.elementKey ?: return
        if (elementKey in deleted) {
            lifecycleScope.launch { closeBottomSheet() }
        }
    }

    @AnyThread
    override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        val elementKey = (shownBottomSheet.value as? BottomSheetContent.Overlay)?.elementKey ?: return
        lifecycleScope.launch {
            val openElement = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) }
            // open element does not exist anymore after download
            if (openElement == null) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onCleared() {
        val elementKey = (shownBottomSheet.value as? BottomSheetContent.Overlay)?.elementKey ?: return
        lifecycleScope.launch { closeBottomSheet() }
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++ VIEW CONTROL ++++++++++++++++++++++++++++++++++++++ */

    //region Location - Request location and update location status

    private fun updateLocationAvailability(isAvailable: Boolean) {
        if (isAvailable) {
            onLocationIsEnabled()
        } else {
            onLocationIsDisabled()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationIsEnabled() {
        viewModel.locationState.value = LocationState.SEARCHING
        mapFragment?.startPositionTracking()
        questAutoSyncer.startPositionTracking()

        mapFragment?.centerCurrentPositionIfFollowing()
        locationManager.getCurrentLocation()
    }

    private fun onLocationIsDisabled() {
        viewModel.locationState.value = when {
            hasLocationPermission -> LocationState.ALLOWED
            else -> LocationState.DENIED
        }
        viewModel.isNavigationMode.value = false
        viewModel.displayedPosition.value = null
        mapFragment?.clearPositionTracking()
        questAutoSyncer.stopPositionTracking()
        locationManager.removeUpdates()
    }

    private fun onLocationChanged(location: Location) {
        viewModel.locationState.value = LocationState.UPDATING
    }

    //endregion

    //region Buttons - Functionality for the buttons in the main view

    private fun onClickDownload() {
        if (viewModel.isConnected) {
            val downloadBbox = getDownloadArea() ?: return
            if (viewModel.isUserInitiatedDownloadInProgress) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.confirmation_cancel_prev_download_title)
                    .setPositiveButton(R.string.confirmation_cancel_prev_download_confirmed) { _, _ ->
                        viewModel.download(downloadBbox)
                    }
                    .setNegativeButton(R.string.confirmation_cancel_prev_download_cancel, null)
                    .show()
            } else {
                viewModel.download(downloadBbox)
            }
        } else {
            toast(R.string.offline)
        }
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
        val pos = mapFragment.displayedLocation?.toLatLon() ?: return
        composeNote(pos, true)
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

        when {
            !viewModel.locationState.value.isEnabled -> {
                requestLocation()
            }
            !mapFragment.isFollowingPosition -> {
                setIsFollowingPosition(true)
            }
            else -> {
                setIsNavigationMode(!mapFragment.isNavigationMode)
            }
        }
    }

    private fun onClickLocationPointer() {
        setIsFollowingPosition(true)
    }

    private fun requestLocation() {
        (supportFragmentManager.findFragmentByTag(TAG_LOCATION_REQUEST) as? LocationRequestFragment)?.startRequest()
    }

    private fun onClickCreateButton() {
        TODO()
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

    private fun composeNote(pos: LatLon, isGpxAttached: Boolean = false) {
        showInBottomSheet(BottomSheetContent.CreateNote(isGpxAttached))

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

    /** Close bottom sheet, clear associated highlighting on the map and return to the previous
     *  view (e.g. if it was zoomed in before to focus on an element) */
    @UiThread
    private fun closeBottomSheet() {
        shownBottomSheet.value = null
        clearHighlighting()
        unfreezeMap()
        mapFragment?.endFocus()
    }

    /** Open or replace the bottom sheet. If the bottom sheet is replaces, no appear animation is
     *  played and the highlighting of the previous bottom sheet is cleared. */
    private fun showInBottomSheet(content: BottomSheetContent, clearPreviousHighlighting: Boolean = true) {
        freezeMap()
        if (clearPreviousHighlighting) clearHighlighting()
        shownBottomSheet.value = content
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
            position = getCrosshairPoint()?.let { mapFragment.getPositionAt(it) }
            padding = getOpenQuestFormMapPadding()
        }
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private suspend fun showOverlayElementDetailsOnMap(overlay: Overlay, element: Element, geometry: ElementGeometry) {
        val mapFragment = mapFragment ?: return

        mapFragment.highlightGeometry(geometry)
        mapFragment.highlightPins(overlay.icon.toAndroidResourceId()!!, listOf(geometry.center))
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private fun showQuestDetailsOnMap(quest: Quest, element: Element) {
        val mapFragment = mapFragment ?: return

        if (quest is OsmQuest) {
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

            withContext(Dispatchers.Main) { mapFragment?.putMarkersForCurrentHighlighting(markers) }
        }
    }

    private fun getCrosshairPoint(): PointF? {
        val windowInfo = windowInfo ?: return null
        val padding = getOpenQuestFormMapPadding() ?: return null
        val size = windowInfo.containerSize
        return PointF(
            (padding.left + (size.width - padding.left - padding.right) / 2).toFloat(),
            (padding.top + (size.height - padding.top - padding.bottom) / 2).toFloat()
        )
    }

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

    //region Animation - Animation(s) for when a quest is solved

    private fun showQuestSolvedAnimation(iconResId: Int, position: LatLon) {
        val offset = binding.root.getLocationInWindow()
        val startPos = mapFragment?.getPointOf(position) ?: return

        startPos.x += offset.x
        startPos.y += offset.y

        lastQuestSolved.value = QuestSolvedEvent(iconResId, Offset(startPos.x, startPos.y))
    }

    //endregion

    companion object {
        private const val TAG_LOCATION_REQUEST = "LocationRequestFragment"
    }
}

@Serializable
sealed interface BottomSheetContent {
    data class Quest(val questKey: QuestKey) : BottomSheetContent
    data class Overlay(val elementKey: ElementKey) : BottomSheetContent
    data class CreateNote(val isGpxAttached: Boolean) : BottomSheetContent
    // TODO: SplitWay etc. actually also has elementKey... (i.e. form should also close when element is deleted server-side)
}
