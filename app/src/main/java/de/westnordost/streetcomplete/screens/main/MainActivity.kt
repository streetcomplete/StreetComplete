package de.westnordost.streetcomplete.screens.main

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PointF
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.AnyThread
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.compose.ui.geometry.Offset
import androidx.core.graphics.Insets
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuestsHiddenSource
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestAutoSyncer
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.databinding.ActivityMainBinding
import de.westnordost.streetcomplete.databinding.EffectQuestPlopBinding
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.osm.level.parseLevelsOrNull
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.IsShowingElement
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.IsShowingQuestDetails
import de.westnordost.streetcomplete.quests.LeaveNoteInsteadFragment
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm
import de.westnordost.streetcomplete.screens.BaseActivity
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreateNoteFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MoveNodeFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.SplitWayFragment
import de.westnordost.streetcomplete.screens.main.controls.LocationState
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryViewModel
import de.westnordost.streetcomplete.screens.main.edithistory.icon
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.MapFragment
import de.westnordost.streetcomplete.screens.main.map.Marker
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.screens.main.map.getIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.screens.main.map.maplibre.CameraPosition
import de.westnordost.streetcomplete.screens.main.map.maplibre.toPadding
import de.westnordost.streetcomplete.ui.util.content
import de.westnordost.streetcomplete.util.SoundFx
import de.westnordost.streetcomplete.util.buildGeoUri
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.isLocationAvailable
import de.westnordost.streetcomplete.util.ktx.observe
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.truncateTo5Decimals
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.qualifier.named
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.random.Random

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
    AbstractOsmQuestForm.Listener,
    AbstractOverlayForm.Listener,
    SplitWayFragment.Listener,
    NoteDiscussionForm.Listener,
    LeaveNoteInsteadFragment.Listener,
    CreateNoteFragment.Listener,
    MoveNodeFragment.Listener,
    // listeners to changes to data:
    VisibleQuestsSource.Listener,
    MapDataWithEditsSource.Listener,
    // rest
    ShowsGeometryMarkers {

    private val questAutoSyncer: QuestAutoSyncer by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val prefs: Preferences by inject()
    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val notesSource: NotesWithEditsSource by inject()
    private val noteQuestsHiddenSource: OsmNoteQuestsHiddenSource by inject()
    private val featureDictionary: Lazy<FeatureDictionary> by inject(named("FeatureDictionaryLazy"))
    private val soundFx: SoundFx by inject()

    private lateinit var locationManager: FineLocationManager

    private val viewModel by viewModel<MainViewModel>()
    private val editHistoryViewModel by viewModel<EditHistoryViewModel>()

    private lateinit var binding: ActivityMainBinding

    private var wasFollowingPosition: Boolean? = null
    private var wasNavigationMode: Boolean? = null

    private val mapFragment: MainMapFragment? get() =
        supportFragmentManager.findFragmentById(R.id.mapFragment) as MainMapFragment?

    private val bottomSheetFragment: Fragment? get() =
        supportFragmentManager.findFragmentByTag(BOTTOM_SHEET)

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    private val sheetBackPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            (bottomSheetFragment as IsCloseableBottomSheet).onClickClose { closeBottomSheet() }
        }
    }

    private val requestLocationPermissionResultReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (!intent.getBooleanExtra(LocationRequestFragment.GRANTED, false)) {
                toast(R.string.no_gps_no_quests, Toast.LENGTH_LONG)
            }
        }
    }

    //region Lifecycle - Android Lifecycle Callbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        val systemBarStyle = SystemBarStyle.dark(Color.argb(0x80, 0x1b, 0x1b, 0x1b))
        enableEdgeToEdge(systemBarStyle, systemBarStyle)
        super.onCreate(savedInstanceState)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            requestLocationPermissionResultReceiver,
            IntentFilter(LocationRequestFragment.REQUEST_LOCATION_PERMISSION_RESULT)
        )
        supportFragmentManager.commit { add(LocationRequestFragment(), TAG_LOCATION_REQUEST) }

        lifecycle.addObserver(questAutoSyncer)

        locationManager = FineLocationManager(this, this::onLocationChanged)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.controls.content {
            MainScreen(
                viewModel = viewModel,
                editHistoryViewModel = editHistoryViewModel,
                onClickZoomIn = ::onClickZoomIn,
                onClickZoomOut = ::onClickZoomOut,
                onClickCompass = ::onClickCompassButton,
                onClickLocation = ::onClickLocationButton,
                onClickLocationPointer = ::onClickLocationPointer,
                onClickCreate = ::onClickCreateButton,
                onClickStopTrackRecording = ::onClickTracksStop,
                onClickDownload = ::onClickDownload,
                onExplainedNeedForLocationPermission = ::requestLocation
            )
        }

        onBackPressedDispatcher.addCallback(this, sheetBackPressedCallback)
        sheetBackPressedCallback.isEnabled = bottomSheetFragment is IsCloseableBottomSheet

        observe(editHistoryViewModel.selectedEdit) { edit ->
            if (edit != null) {
                val geometry = editHistoryViewModel.getEditGeometry(edit)
                mapFragment?.startFocus(geometry, Insets.NONE)
                mapFragment?.highlightGeometry(geometry)
                mapFragment?.highlightPins(edit.icon, listOf(edit.position))
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
                mapFragment?.setInitialCameraPosition(geoUri)
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

        wasFollowingPosition = mapFragment?.isFollowingPosition
        wasNavigationMode = mapFragment?.isNavigationMode

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
        viewModel.geoUri.value?.let { mapFragment?.setInitialCameraPosition(it) }
        viewModel.isFollowingPosition.value = mapFragment?.isFollowingPosition ?: false
        viewModel.isNavigationMode.value = mapFragment?.isNavigationMode ?: false
        viewModel.isRecordingTracks.value = mapFragment?.isRecordingTracks ?: false
        viewModel.mapCamera.value = mapFragment?.cameraPosition
        updateDisplayedPosition()
    }

    override fun onMapIsChanging(camera: CameraPosition) {
        viewModel.mapCamera.value = camera
        updateDisplayedPosition()

        val f = bottomSheetFragment
        if (f is IsMapOrientationAware) f.onMapOrientation(camera.rotation, camera.tilt)
        if (f is IsMapPositionAware) f.onMapMoved(camera.position)
    }

    override fun onPanBegin() {
        /* panning only results in not following location anymore if a location is already known
           and displayed
         */
        if (mapFragment?.displayedLocation != null) {
            setIsFollowingPosition(false)
        }
    }

    override fun onLongPress(point: PointF, position: LatLon) {
        if (bottomSheetFragment != null || editHistoryViewModel.isShowingSidebar.value) return

        binding.contextMenuView.translationX = point.x
        binding.contextMenuView.translationY = point.y

        showMapContextMenu(position)
    }

    /* ---------------------------- MainMapFragment.Listener --------------------------- */

    override fun onClickedQuest(questKey: QuestKey) {
        if (isQuestDetailsCurrentlyDisplayedFor(questKey)) return
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { lifecycleScope.launch { showQuestDetails(questKey) } }
        } else {
            lifecycleScope.launch { showQuestDetails(questKey) }
        }
    }

    override fun onClickedEdit(editKey: EditKey) {
        editHistoryViewModel.select(editKey)
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            if (!f.onClickMapAt(position, clickAreaSizeInMeters)) {
                f.onClickClose { closeBottomSheet() }
            }
        } else if (editHistoryViewModel.isShowingSidebar.value) {
            editHistoryViewModel.hideSidebar()
        }
    }

    override fun onClickedElement(elementKey: ElementKey) {
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { lifecycleScope.launch { showElementDetails(elementKey) } }
        } else {
            lifecycleScope.launch { showElementDetails(elementKey) }
        }
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

    //region Bottom Sheet - Callbacks from the bottom sheet (quest forms, split way form, create note form, ...)

    /* ------------------------------- AbstractOsmQuestForm.Listener ---------------------------- */
    /* -------------------------------- AbstractOverlayForm.Listener ---------------------------- */

    override val displayedMapLocation: Location? get() = mapFragment?.displayedLocation

    override val metersPerPixel: Double? get() = mapFragment?.getMetersPerPixel()

    override fun onEdited(editType: ElementEditType, geometry: ElementGeometry) {
        showQuestSolvedAnimation(editType.icon, geometry.center)
        closeBottomSheet()
    }

    override fun onComposeNote(editType: ElementEditType, element: Element, geometry: ElementGeometry, leaveNoteContext: String) {
        showInBottomSheet(
            LeaveNoteInsteadFragment.create(element.type, element.id, leaveNoteContext, geometry.center),
            false
        )
    }

    override fun onSplitWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry) {
        val mapFragment = mapFragment ?: return
        showInBottomSheet(SplitWayFragment.create(editType, way, geometry))
        mapFragment.highlightGeometry(geometry)
        mapFragment.hideNonHighlightedPins()
        mapFragment.hideOverlay()
    }

    override fun onQuestHidden(osmQuestKey: OsmQuestKey) {
        closeBottomSheet()
    }

    override fun getPointOf(pos: LatLon): PointF? =
        mapFragment?.getPointOf(pos)

    /* ------------------------------- SplitWayFragment.Listener -------------------------------- */

    override fun onSplittedWay(editType: ElementEditType, way: Way, geometry: ElementPolylinesGeometry) {
        showQuestSolvedAnimation(editType.icon, geometry.center)
        closeBottomSheet()
    }

    /* ------------------------------- MoveNodeFragment.Listener -------------------------------- */

    override fun onMoveNode(editType: ElementEditType, node: Node) {
        val mapFragment = mapFragment ?: return
        showInBottomSheet(MoveNodeFragment.create(editType, node), clearPreviousHighlighting = false)
        mapFragment.clearSelectedPins()
        mapFragment.hideNonHighlightedPins()
        if (editType !is Overlay) {
            mapFragment.hideOverlay()
        }
        mapFragment.updateCameraPosition {
            position = node.position
            padding = getQuestFormInsets().toPadding()
        }
    }

    override fun onMovedNode(editType: ElementEditType, position: LatLon) {
        showQuestSolvedAnimation(editType.icon, position)
        closeBottomSheet()
    }

    override fun getScreenPositionAt(mapPos: LatLon): PointF? =
        mapFragment?.getPointOf(mapPos)

    /* ------------------------------- ShowsPointMarkers -------------------------------- */

    override fun putMarkersForCurrentHighlighting(markers: Iterable<Marker>) {
        mapFragment?.putMarkersForCurrentHighlighting(markers)
    }

    @UiThread
    override fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry) {
        mapFragment?.deleteMarkerForCurrentHighlighting(geometry)
    }

    @UiThread
    override fun clearMarkersForCurrentHighlighting() {
        mapFragment?.clearMarkersForCurrentHighlighting()
    }

    /* ------------------------------ NoteDiscussionForm.Listener ------------------------------- */

    override fun onNoteQuestSolved(questType: QuestType, noteId: Long, position: LatLon) {
        showQuestSolvedAnimation(questType.icon, position)
        closeBottomSheet()
    }

    override fun onNoteQuestClosed() {
        closeBottomSheet()
    }

    /* ------------------------------- CreateNoteFragment.Listener ------------------------------ */

    override fun onCreatedNote(position: LatLon) {
        showQuestSolvedAnimation(R.drawable.ic_quest_create_note, position)
        closeBottomSheet()
    }

    override fun getMapPositionAt(screenPos: PointF): LatLon? =
        mapFragment?.getPositionAt(screenPos)

    override fun getRecordedTrack(): List<Trackpoint>? =
        mapFragment?.recordedTracks

    private fun getQuestFormInsets() = Insets.of(
        resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
        resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
        resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
        resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
    )

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread
    override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
        lifecycleScope.launch {
            val f = bottomSheetFragment
            // open quest has been deleted
            if (f is IsShowingQuestDetails && f.questKey in removed) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onVisibleQuestsInvalidated() {
        lifecycleScope.launch {
            val f = bottomSheetFragment
            if (f is IsShowingQuestDetails) {
                val openQuest = withContext(Dispatchers.IO) { visibleQuestsSource.get(f.questKey) }
                // open quest does not exist anymore after visible quest invalidation
                if (openQuest == null) closeBottomSheet()
            }
        }
    }

    /* ---------------------------- MapDataWithEditsSource.Listener ----------------------------- */

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        lifecycleScope.launch {
            val f = bottomSheetFragment
            // open element has been deleted
            if (f is IsShowingElement && f.elementKey in deleted) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        lifecycleScope.launch {
            val f = bottomSheetFragment
            if (f !is IsShowingElement) return@launch
            val elementKey = f.elementKey ?: return@launch
            val openElement = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) }
            // open element does not exist anymore after download
            if (openElement == null) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onCleared() {
        lifecycleScope.launch {
            val f = bottomSheetFragment
            if (f is IsShowingElement) {
                closeBottomSheet()
            }
        }
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

        setIsFollowingPosition(wasFollowingPosition ?: true)
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
        showOverlayFormForNewElement()
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

    private fun showMapContextMenu(position: LatLon) {
        val popupMenu = PopupMenu(this, binding.contextMenuView)
        popupMenu.inflate(R.menu.menu_map_context)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_create_note -> onClickCreateNote(position)
                R.id.action_create_track -> onClickCreateTrack()
                R.id.action_open_location -> onClickOpenLocationInOtherApp(position)
            }
            true
        }
        popupMenu.show()
    }

    private fun onClickOpenLocationInOtherApp(pos: LatLon) {
        val zoom = mapFragment?.cameraPosition?.zoom
        val uri = buildGeoUri(pos.latitude, pos.longitude, zoom)

        val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
        val otherMapAppInstalled = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .any { !it.activityInfo.packageName.equals(packageName) }
        if (otherMapAppInstalled) {
            startActivity(intent)
        } else {
            toast(R.string.map_application_missing, Toast.LENGTH_LONG)
        }
    }

    private fun onClickCreateNote(pos: LatLon) {
        if ((mapFragment?.cameraPosition?.zoom ?: 0.0) < ApplicationConstants.NOTE_MIN_ZOOM) {
            toast(R.string.create_new_note_unprecise)
            return
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { composeNote(pos) }
        } else {
            composeNote(pos)
        }
    }

    private fun composeNote(pos: LatLon, hasGpxAttached: Boolean = false) {
        showInBottomSheet(CreateNoteFragment.create(hasGpxAttached))
        mapFragment?.updateCameraPosition(300) {
            position = pos
            padding = getQuestFormInsets().toPadding()
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
        currentFocus?.hideKeyboard()
        if (bottomSheetFragment != null) {
            supportFragmentManager.popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        clearHighlighting()
        unfreezeMap()
        mapFragment?.endFocus()
        sheetBackPressedCallback.isEnabled = false
    }

    /** Open or replace the bottom sheet. If the bottom sheet is replaces, no appear animation is
     *  played and the highlighting of the previous bottom sheet is cleared. */
    private fun showInBottomSheet(f: Fragment, clearPreviousHighlighting: Boolean = true) {
        currentFocus?.hideKeyboard()
        freezeMap()
        if (bottomSheetFragment != null) {
            if (clearPreviousHighlighting) clearHighlighting()
            supportFragmentManager.popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        val appearAnim = R.animator.quest_answer_form_appear
        val disappearAnim = R.animator.quest_answer_form_disappear
        supportFragmentManager.commit(true) {
            setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
            add(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET)
            addToBackStack(BOTTOM_SHEET)
        }
        sheetBackPressedCallback.isEnabled = f is IsCloseableBottomSheet
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
    private fun showOverlayFormForNewElement() {
        val overlay = viewModel.selectedOverlay.value ?: return
        val mapFragment = mapFragment ?: return

        val f = overlay.createForm(null) ?: return
        if (f.arguments == null) f.arguments = bundleOf()
        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0.0
        val tilt = camera?.tilt ?: 0.0
        val args = AbstractOverlayForm.createArguments(overlay, null, null, rotation, tilt)
        f.requireArguments().putAll(args)

        showInBottomSheet(f)
        val pos = getCrosshairPoint()?.let { getMapPositionAt(it) }
        mapFragment.updateCameraPosition {
            position = pos
            padding = getQuestFormInsets().toPadding()
        }
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private suspend fun showElementDetails(elementKey: ElementKey) {
        if (isElementCurrentlyDisplayed(elementKey)) return
        val overlay = viewModel.selectedOverlay.value ?: return
        val geometry = mapDataWithEditsSource.getGeometry(elementKey.type, elementKey.id) ?: return
        val mapFragment = mapFragment ?: return

        // open note if it is blocking element
        val center = geometry.center
        val note = withContext(Dispatchers.IO) {
            notesSource
                .getAll(BoundingBox(center, center).enlargedBy(1.2))
                .firstOrNull { it.position.truncateTo5Decimals() == center.truncateTo5Decimals() }
                ?.takeIf { noteQuestsHiddenSource.getHidden(it.id) == null }
        }
        if (note != null) {
            showQuestDetails(OsmNoteQuest(note.id, note.position))
            return
        }

        val element = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) } ?: return
        val f = overlay.createForm(element) ?: return
        if (f.arguments == null) f.arguments = bundleOf()

        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0.0
        val tilt = camera?.tilt ?: 0.0
        val args = AbstractOverlayForm.createArguments(overlay, element, geometry, rotation, tilt)
        f.requireArguments().putAll(args)

        showInBottomSheet(f)

        mapFragment.highlightGeometry(geometry)
        mapFragment.highlightPins(overlay.icon, listOf(geometry.center))
        mapFragment.hideNonHighlightedPins()
    }

    private fun isElementCurrentlyDisplayed(elementKey: ElementKey): Boolean {
        val f = bottomSheetFragment
        if (f !is IsShowingElement) return false
        return f.elementKey == elementKey
    }

    private suspend fun showQuestDetails(questKey: QuestKey) {
        val quest = visibleQuestsSource.get(questKey)
        if (quest != null) {
            showQuestDetails(quest)
        }
    }

    @UiThread
    private suspend fun showQuestDetails(quest: Quest) {
        val mapFragment = mapFragment ?: return
        if (isQuestDetailsCurrentlyDisplayedFor(quest.key)) return

        val f = quest.type.createForm()
        if (f.arguments == null) f.arguments = bundleOf()

        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0.0
        val tilt = camera?.tilt ?: 0.0
        val args = AbstractQuestForm.createArguments(quest.key, quest.type, quest.geometry, rotation, tilt)
        f.requireArguments().putAll(args)

        if (quest is OsmQuest) {
            val element = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(quest.elementType, quest.elementId) } ?: return
            val osmArgs = AbstractOsmQuestForm.createArguments(element)
            f.requireArguments().putAll(osmArgs)
            showHighlightedElements(quest, element)
        }

        showInBottomSheet(f)

        mapFragment.startFocus(quest.geometry, getQuestFormInsets())
        mapFragment.highlightGeometry(quest.geometry)
        mapFragment.highlightPins(quest.type.icon, quest.markerLocations)
        mapFragment.hideNonHighlightedPins(quest.key)
        mapFragment.hideOverlay()
    }

    private fun showHighlightedElements(quest: OsmQuest, element: Element) {
        val bbox = quest.geometry.getBounds().enlargedBy(quest.type.highlightedElementsRadius)
        var mapData: MapDataWithGeometry? = null

        fun getMapData(): MapDataWithGeometry {
            val data = mapDataWithEditsSource.getMapDataWithGeometry(bbox)
            mapData = data
            return data
        }

        val levels = parseLevelsOrNull(element.tags)

        lifecycleScope.launch(Dispatchers.Default) {
            val elements = withContext(Dispatchers.IO) {
                quest.type.getHighlightedElements(element, ::getMapData)
            }

            val markers = elements.mapNotNull { e ->
                // don't highlight "this" element
                if (element == e) return@mapNotNull null
                // include only elements with the same (=intersecting) level, if any
                val eLevels = parseLevelsOrNull(e.tags)
                if (!levels.levelsIntersect(eLevels)) return@mapNotNull null
                // include only elements with the same layer, if any
                if (element.tags["layer"] != e.tags["layer"]) return@mapNotNull null

                val geometry = mapData?.getGeometry(e.type, e.id) ?: return@mapNotNull null
                val icon = getIcon(featureDictionary.value, e)
                val title = getTitle(e.tags)
                Marker(geometry, icon, title)
            }.toList()

            withContext(Dispatchers.Main) { putMarkersForCurrentHighlighting(markers) }
        }
    }

    private fun isQuestDetailsCurrentlyDisplayedFor(questKey: QuestKey): Boolean {
        val f = bottomSheetFragment
        return f is IsShowingQuestDetails && f.questKey == questKey
    }

    private fun getCrosshairPoint(): PointF {
        val view = binding.root
        val left = resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset)
        val right = resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset)
        val top = resources.getDimensionPixelSize(R.dimen.quest_form_topOffset)
        val bottom = resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        val x = (view.width + left - right) / 2f
        val y = (view.height + top - bottom) / 2f
        return PointF(x, y)
    }

    //endregion

    //region Animation - Animation(s) for when a quest is solved

    private fun showQuestSolvedAnimation(iconResId: Int, position: LatLon) {
        val offset = binding.root.getLocationInWindow()
        val startPos = mapFragment?.getPointOf(position) ?: return

        val size = resources.dpToPx(42).toInt()
        startPos.x += offset.x - size / 2f
        startPos.y += offset.y - size * 1.5f

        showMarkerSolvedAnimation(iconResId, startPos)
    }

    private fun showMarkerSolvedAnimation(@DrawableRes iconResId: Int, startScreenPos: PointF) {
        lifecycleScope.launch {
            soundFx.play(resources.getIdentifier("plop" + Random.nextInt(4), "raw", packageName))
        }

        val root = window.decorView as ViewGroup
        val img = EffectQuestPlopBinding.inflate(layoutInflater, root, false).root
        img.x = startScreenPos.x
        img.y = startScreenPos.y
        img.setImageResource(iconResId)
        root.addView(img)

        flingQuestMarker(img) { root.removeView(img) }
    }

    private fun flingQuestMarker(quest: View, onFinished: () -> Unit) {
        quest.animate()
            .scaleX(1.6f).scaleY(1.6f)
            .setInterpolator(OvershootInterpolator(8f))
            .setDuration(250)
            .withEndAction {
                quest.animate()
                    .scaleX(0.2f).scaleY(0.2f)
                    .alpha(0.8f)
                    .x(0f).y(0f)
                    .setDuration(250)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction(onFinished)
            }
    }

    //endregion

    companion object {
        private const val BOTTOM_SHEET = "bottom_sheet"

        private const val TAG_LOCATION_REQUEST = "LocationRequestFragment"
    }
}
