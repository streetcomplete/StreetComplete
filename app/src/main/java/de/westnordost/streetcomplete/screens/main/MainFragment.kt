package de.westnordost.streetcomplete.screens.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.location.Location
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.annotation.AnyThread
import androidx.annotation.DrawableRes
import androidx.annotation.UiThread
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.Insets
import androidx.core.graphics.minus
import androidx.core.graphics.toPointF
import androidx.core.graphics.toRectF
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.download.tiles.asBoundingBoxOfEnclosingTiles
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.edithistory.icon
import de.westnordost.streetcomplete.data.osm.edits.ElementEdit
import de.westnordost.streetcomplete.data.osm.edits.ElementEditType
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Node
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuestHidden
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.osmnotes.notequests.OsmNoteQuest
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.data.overlays.SelectedOverlaySource
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.Quest
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.VisibleQuestsSource
import de.westnordost.streetcomplete.databinding.EffectQuestPlopBinding
import de.westnordost.streetcomplete.databinding.FragmentMainBinding
import de.westnordost.streetcomplete.osm.level.createLevelsOrNull
import de.westnordost.streetcomplete.osm.level.levelsIntersect
import de.westnordost.streetcomplete.overlays.AbstractOverlayForm
import de.westnordost.streetcomplete.overlays.IsShowingElement
import de.westnordost.streetcomplete.overlays.Overlay
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AbstractQuestForm
import de.westnordost.streetcomplete.quests.IsShowingQuestDetails
import de.westnordost.streetcomplete.quests.LeaveNoteInsteadFragment
import de.westnordost.streetcomplete.quests.note_discussion.NoteDiscussionForm
import de.westnordost.streetcomplete.screens.HandlesOnBackPressed
import de.westnordost.streetcomplete.screens.MainActivity
import de.westnordost.streetcomplete.screens.main.bottom_sheet.CreateNoteFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsCloseableBottomSheet
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapOrientationAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.IsMapPositionAware
import de.westnordost.streetcomplete.screens.main.bottom_sheet.MoveNodeFragment
import de.westnordost.streetcomplete.screens.main.bottom_sheet.SplitWayFragment
import de.westnordost.streetcomplete.screens.main.controls.LocationStateButton
import de.westnordost.streetcomplete.screens.main.controls.MainMenuButtonFragment
import de.westnordost.streetcomplete.screens.main.controls.UndoButtonFragment
import de.westnordost.streetcomplete.screens.main.edithistory.EditHistoryFragment
import de.westnordost.streetcomplete.screens.main.map.LocationAwareMapFragment
import de.westnordost.streetcomplete.screens.main.map.MainMapFragment
import de.westnordost.streetcomplete.screens.main.map.MapFragment
import de.westnordost.streetcomplete.screens.main.map.ShowsGeometryMarkers
import de.westnordost.streetcomplete.screens.main.map.getPinIcon
import de.westnordost.streetcomplete.screens.main.map.getTitle
import de.westnordost.streetcomplete.screens.main.map.tangram.CameraPosition
import de.westnordost.streetcomplete.util.SoundFx
import de.westnordost.streetcomplete.util.buildGeoUri
import de.westnordost.streetcomplete.util.ktx.childFragmentManagerOrNull
import de.westnordost.streetcomplete.util.ktx.dpToPx
import de.westnordost.streetcomplete.util.ktx.getLocationInWindow
import de.westnordost.streetcomplete.util.ktx.hasLocationPermission
import de.westnordost.streetcomplete.util.ktx.hideKeyboard
import de.westnordost.streetcomplete.util.ktx.isLocationEnabled
import de.westnordost.streetcomplete.util.ktx.setMargins
import de.westnordost.streetcomplete.util.ktx.toLatLon
import de.westnordost.streetcomplete.util.ktx.toast
import de.westnordost.streetcomplete.util.ktx.viewLifecycleScope
import de.westnordost.streetcomplete.util.location.FineLocationManager
import de.westnordost.streetcomplete.util.location.LocationAvailabilityReceiver
import de.westnordost.streetcomplete.util.location.LocationRequestFragment
import de.westnordost.streetcomplete.util.math.area
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.enlargedBy
import de.westnordost.streetcomplete.util.math.initialBearingTo
import de.westnordost.streetcomplete.util.viewBinding
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** This fragment controls the main view.
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
 *  */
class MainFragment :
    Fragment(R.layout.fragment_main),
    // listeners to child fragments:
    MapFragment.Listener,
    LocationAwareMapFragment.Listener,
    MainMapFragment.Listener,
    AbstractOsmQuestForm.Listener,
    AbstractOverlayForm.Listener,
    SplitWayFragment.Listener,
    NoteDiscussionForm.Listener,
    LeaveNoteInsteadFragment.Listener,
    CreateNoteFragment.Listener,
    MoveNodeFragment.Listener,
    EditHistoryFragment.Listener,
    MainMenuButtonFragment.Listener,
    UndoButtonFragment.Listener,
    // listeners to changes to data:
    VisibleQuestsSource.Listener,
    MapDataWithEditsSource.Listener,
    SelectedOverlaySource.Listener,
    // rest
    HandlesOnBackPressed,
    ShowsGeometryMarkers {

    private val visibleQuestsSource: VisibleQuestsSource by inject()
    private val mapDataWithEditsSource: MapDataWithEditsSource by inject()
    private val notesSource: NotesWithEditsSource by inject()
    private val locationAvailabilityReceiver: LocationAvailabilityReceiver by inject()
    private val selectedOverlaySource: SelectedOverlaySource by inject()
    private val soundFx: SoundFx by inject()
    private val prefs: SharedPreferences by inject()

    private lateinit var locationManager: FineLocationManager

    private val binding by viewBinding(FragmentMainBinding::bind)

    private var wasFollowingPosition: Boolean? = null
    private var wasNavigationMode: Boolean? = null

    private var windowInsets: Insets? = null

    private var mapFragment: MainMapFragment? = null
    private var mainMenuButtonFragment: MainMenuButtonFragment? = null

    private val bottomSheetFragment: Fragment? get() =
        childFragmentManagerOrNull?.findFragmentByTag(BOTTOM_SHEET)

    private val editHistoryFragment: EditHistoryFragment? get() =
        childFragmentManagerOrNull?.findFragmentByTag(EDIT_HISTORY) as? EditHistoryFragment

    private var mapOffsetWithOpenBottomSheet: RectF = RectF(0f, 0f, 0f, 0f)

    interface Listener {
        fun onMapInitialized()
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    //region Lifecycle - Android Lifecycle Callbacks

    override fun onAttach(context: Context) {
        super.onAttach(context)

        locationManager = FineLocationManager(context, this::onLocationChanged)

        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            when (fragment) {
                is MainMapFragment -> mapFragment = fragment
                is MainMenuButtonFragment -> mainMenuButtonFragment = fragment
            }
        }
        childFragmentManager.commit { add(LocationRequestFragment(), TAG_LOCATION_REQUEST) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapControls.respectSystemInsets(View::setMargins)
        view.respectSystemInsets { windowInsets = it }

        updateCreateButtonVisibility()

        binding.locationPointerPin.setOnClickListener { onClickLocationPointer() }

        binding.compassView.setOnClickListener { onClickCompassButton() }
        binding.gpsTrackingButton.setOnClickListener { onClickTrackingButton() }
        binding.stopTracksButton.setOnClickListener { onClickTracksStop() }
        binding.zoomInButton.setOnClickListener { onClickZoomIn() }
        binding.zoomOutButton.setOnClickListener { onClickZoomOut() }
        binding.createButton.setOnClickListener { onClickCreateButton() }

        updateOffsetWithOpenBottomSheet()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mapFragment = this.mapFragment ?: return
        /* when rotating the screen and the bottom sheet is open, the view
           should not rotate around its proper center but around the center
           of the part of the map that is not occluded by the bottom sheet */
        val previousOffset = mapOffsetWithOpenBottomSheet
        updateOffsetWithOpenBottomSheet()
        if (bottomSheetFragment != null) {
            mapFragment.adjustToOffsets(previousOffset, mapOffsetWithOpenBottomSheet)
        }
        binding.crosshairView.setPadding(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        )
        updateLocationPointerPin()
    }

    override fun onStart() {
        super.onStart()
        visibleQuestsSource.addListener(this)
        mapDataWithEditsSource.addListener(this)
        selectedOverlaySource.addListener(this)
        locationAvailabilityReceiver.addListener(::updateLocationAvailability)
        updateLocationAvailability(requireContext().run { hasLocationPermission && isLocationEnabled })
    }

    /** Called by the activity when the user presses the back button.
     *  Returns true if the event should be consumed. */
    override fun onBackPressed(): Boolean {
        if (editHistoryFragment != null) {
            closeEditHistorySidebar()
            return true
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { closeBottomSheet() }
            return true
        }

        return false
    }

    override fun onStop() {
        super.onStop()
        wasFollowingPosition = mapFragment?.isFollowingPosition
        wasNavigationMode = mapFragment?.isNavigationMode
        visibleQuestsSource.removeListener(this)
        locationAvailabilityReceiver.removeListener(::updateLocationAvailability)
        mapDataWithEditsSource.removeListener(this)
        selectedOverlaySource.removeListener(this)
        locationManager.removeUpdates()
    }

    private fun updateOffsetWithOpenBottomSheet() {
        mapOffsetWithOpenBottomSheet = Rect(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_topOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        ).toRectF()
    }

    //endregion

    //region QuestsMapFragment - Callbacks from the map with its quest pins

    /* ---------------------------------- MapFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        binding.gpsTrackingButton.isActivated = mapFragment?.isFollowingPosition ?: false
        binding.gpsTrackingButton.isNavigation = mapFragment?.isNavigationMode ?: false
        binding.stopTracksButton.isVisible = mapFragment?.isRecordingTracks ?: false
        updateLocationPointerPin()
        mapFragment?.cameraPosition?.zoom?.let { updateCreateButtonEnablement(it) }
        listener?.onMapInitialized()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        binding.compassView.rotation = (180 * rotation / PI).toFloat()
        binding.compassView.rotationX = (180 * tilt / PI).toFloat()

        val margin = 2 * PI / 180
        binding.compassView.isInvisible = abs(rotation) < margin && tilt < margin

        updateLocationPointerPin()
        updateCreateButtonEnablement(zoom)

        val f = bottomSheetFragment
        if (f is IsMapOrientationAware) f.onMapOrientation(rotation, tilt)
        if (f is IsMapPositionAware) f.onMapMoved(position)
    }

    override fun onPanBegin() {
        /* panning only results in not following location anymore if a location is already known
           and displayed
         */
        if (mapFragment?.displayedLocation != null) {
            setIsFollowingPosition(false)
        }
    }

    override fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float) { }

    override fun onLongPress(x: Float, y: Float) {
        val point = PointF(x, y)
        val position = mapFragment?.getPositionAt(point) ?: return
        if (bottomSheetFragment != null || editHistoryFragment != null) return

        binding.contextMenuView.translationX = x
        binding.contextMenuView.translationY = y

        showMapContextMenu(position)
    }

    /* ---------------------------- LocationAwareMapFragment.Listener --------------------------- */

    override fun onDisplayedLocationDidChange() {
        updateLocationPointerPin()
    }

    /* ---------------------------- QuestsMapFragment.Listener --------------------------- */

    override fun onClickedQuest(questKey: QuestKey) {
        if (isQuestDetailsCurrentlyDisplayedFor(questKey)) return
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { viewLifecycleScope.launch { showQuestDetails(questKey) } }
        } else {
            viewLifecycleScope.launch { showQuestDetails(questKey) }
        }
    }

    override fun onClickedEdit(editKey: EditKey) {
        editHistoryFragment?.select(editKey)
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            if (!f.onClickMapAt(position, clickAreaSizeInMeters)) {
                f.onClickClose { closeBottomSheet() }
            }
        } else if (editHistoryFragment != null) {
            closeEditHistorySidebar()
        }
    }

    override fun onClickedElement(elementKey: ElementKey) {
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            f.onClickClose { viewLifecycleScope.launch { showElementDetails(elementKey) } }
        } else {
            viewLifecycleScope.launch { showElementDetails(elementKey) }
        }
    }

    //endregion

    //region Buttons - Callbacks from the buttons in the main view

    /* ---------------------------- MainMenuButtonFragment.Listener ----------------------------- */

    override fun getDownloadArea(): BoundingBox? {
        val displayArea = mapFragment?.getDisplayedArea()
        if (displayArea == null) {
            context?.toast(R.string.cannot_find_bbox_or_reduce_tilt, Toast.LENGTH_LONG)
            return null
        }

        val enclosingBBox = displayArea.asBoundingBoxOfEnclosingTiles(ApplicationConstants.DOWNLOAD_TILE_ZOOM)
        val areaInSqKm = enclosingBBox.area() / 1000000
        if (areaInSqKm > ApplicationConstants.MAX_DOWNLOADABLE_AREA_IN_SQKM) {
            context?.toast(R.string.download_area_too_big, Toast.LENGTH_LONG)
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

    /* ------------------------------ UndoButtonFragment.Listener ------------------------------- */

    override fun onClickShowEditHistory() {
        showEditHistorySidebar()
    }

    //endregion

    //region Bottom Sheet - Callbacks from the bottom sheet (quest forms, split way form, create note form, ...)

    /* ------------------------------- AbstractOsmQuestForm.Listener ---------------------------- */
    /* -------------------------------- AbstractOverlayForm.Listener ---------------------------- */

    override val displayedMapLocation: Location? get() = mapFragment?.displayedLocation

    override fun onEdited(editType: ElementEditType, element: Element, geometry: ElementGeometry) {
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

        mapFragment.show3DBuildings = false
        val offsetPos = mapFragment.getPositionThatCentersPosition(node.position, RectF())
        mapFragment.updateCameraPosition { position = offsetPos }
    }

    override fun onMovedNode(editType: ElementEditType, position: LatLon) {
        showQuestSolvedAnimation(editType.icon, position)
        closeBottomSheet()
    }

    override fun getScreenPositionAt(mapPos: LatLon): PointF? =
        mapFragment?.getPointOf(mapPos)

    /* ------------------------------- ShowsPointMarkers -------------------------------- */

    override fun putMarkerForCurrentHighlighting(
        geometry: ElementGeometry,
        @DrawableRes drawableResId: Int?,
        title: String?
    ) {
        mapFragment?.putMarkerForCurrentHighlighting(geometry, drawableResId, title)
    }

    override fun deleteMarkerForCurrentHighlighting(geometry: ElementGeometry) {
        mapFragment?.deleteMarkerForCurrentHighlighting(geometry)
    }

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

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ------------------------------ SelectedOverlaySource.Listener -----------------------------*/

    override fun onSelectedOverlayChanged() {
        updateCreateButtonVisibility()

        val f = bottomSheetFragment
        if (f is IsShowingElement) {
            viewLifecycleScope.launch { closeBottomSheet() }
        }
    }

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread
    override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
        val f = bottomSheetFragment
        // open quest has been deleted
        if (f is IsShowingQuestDetails && f.questKey in removed) {
            viewLifecycleScope.launch { closeBottomSheet() }
        }
    }

    @AnyThread
    override fun onVisibleQuestsInvalidated() {
        val f = bottomSheetFragment
        if (f is IsShowingQuestDetails) {
            viewLifecycleScope.launch {
                val openQuest = withContext(Dispatchers.IO) { visibleQuestsSource.get(f.questKey) }
                // open quest does not exist anymore after visible quest invalidation
                if (openQuest == null) closeBottomSheet()
            }
        }
    }

    /* ---------------------------- MapDataWithEditsSource.Listener ----------------------------- */

    @AnyThread
    override fun onUpdated(updated: MapDataWithGeometry, deleted: Collection<ElementKey>) {
        val f = bottomSheetFragment
        // open element has been deleted
        if (f is IsShowingElement && f.elementKey in deleted) {
            viewLifecycleScope.launch { closeBottomSheet() }
        }
    }

    @AnyThread
    override fun onReplacedForBBox(bbox: BoundingBox, mapDataWithGeometry: MapDataWithGeometry) {
        val f = bottomSheetFragment
        if (f !is IsShowingElement) return
        val elementKey = f.elementKey ?: return
        viewLifecycleScope.launch {
            val openElement = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) }
            // open element does not exist anymore after download
            if (openElement == null) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread
    override fun onCleared() {
        val f = bottomSheetFragment
        if (f is IsShowingElement) {
            viewLifecycleScope.launch { closeBottomSheet() }
        }
    }

    //endregion

    //region Edit History - Callbacks from the Edit History Sidebar

    override fun onSelectedEdit(edit: Edit) {
        val geometry = edit.getGeometry()
        mapFragment?.startFocus(geometry, mapOffsetWithOpenBottomSheet)
        mapFragment?.highlightGeometry(geometry)
        mapFragment?.highlightPins(edit.icon, listOf(edit.position))
        mapFragment?.hideOverlay()
    }

    private fun Edit.getGeometry(): ElementGeometry = when (this) {
        is ElementEdit -> originalGeometry
        is OsmQuestHidden -> mapDataWithEditsSource.getGeometry(elementType, elementId)
        else -> null
    } ?: ElementPointGeometry(position)

    override fun onDeletedSelectedEdit() {
        mapFragment?.endFocus()
        mapFragment?.clearHighlighting()
    }

    override fun onEditHistoryIsEmpty() {
        closeEditHistorySidebar()
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
        binding.gpsTrackingButton.state = LocationStateButton.State.SEARCHING
        mapFragment!!.startPositionTracking()

        setIsFollowingPosition(wasFollowingPosition ?: true)
        locationManager.getCurrentLocation()
    }

    private fun onLocationIsDisabled() {
        binding.gpsTrackingButton.state = when {
            requireContext().hasLocationPermission -> LocationStateButton.State.ALLOWED
            else -> LocationStateButton.State.DENIED
        }
        binding.gpsTrackingButton.isNavigation = false
        binding.locationPointerPin.visibility = View.GONE
        mapFragment!!.clearPositionTracking()
        locationManager.removeUpdates()
    }

    private fun onLocationChanged(location: Location) {
        viewLifecycleScope.launch {
            binding.gpsTrackingButton.state = LocationStateButton.State.UPDATING
            updateLocationPointerPin()
        }
    }

    //endregion

    //region Buttons - Functionality for the buttons in the main view

    fun onClickMainMenu() {
        mainMenuButtonFragment?.onClickMainMenu()
    }

    private fun onClickZoomOut() {
        mapFragment?.updateCameraPosition(300) { zoomBy = -1f }
    }

    private fun onClickZoomIn() {
        mapFragment?.updateCameraPosition(300) { zoomBy = +1f }
    }

    private fun onClickTracksStop() {
        // hide the track information
        binding.stopTracksButton.isVisible = false
        val mapFragment = mapFragment ?: return
        mapFragment.stopPositionTrackRecording()
        val pos = mapFragment.displayedLocation?.toLatLon() ?: return
        composeNote(pos, true)
    }

    private fun onClickCompassButton() {
        /* Clicking the compass button will always rotate the map back to north and remove tilt */
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition ?: return

        // if the user wants to rotate back north, it means he also doesn't want to use nav mode anymore
        if (mapFragment.isNavigationMode) {
            mapFragment.updateCameraPosition(300) { rotation = 0f }
            setIsNavigationMode(false)
        } else {
            mapFragment.updateCameraPosition(300) {
                rotation = 0f
                tilt = 0f
            }
        }
    }

    private fun onClickTrackingButton() {
        val mapFragment = mapFragment ?: return

        when {
            !binding.gpsTrackingButton.state.isEnabled -> {
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

    private fun requestLocation() {
        (childFragmentManager.findFragmentByTag(TAG_LOCATION_REQUEST) as? LocationRequestFragment)?.startRequest()
    }

    private fun onClickCreateButton() {
        showOverlayFormForNewElement()
    }

    private fun updateCreateButtonVisibility() {
        val isCreateNodeEnabled = selectedOverlaySource.selectedOverlay?.isCreateNodeEnabled == true
        binding.createButton.isGone = !isCreateNodeEnabled
        binding.crosshairView.isGone = !isCreateNodeEnabled
    }

    private fun updateCreateButtonEnablement(zoom: Float) {
        binding.createButton.isEnabled = zoom >= 18f
    }

    private fun setIsNavigationMode(navigation: Boolean) {
        val mapFragment = mapFragment ?: return
        mapFragment.isNavigationMode = navigation
        binding.gpsTrackingButton.isNavigation = navigation
        // always re-center position because navigation mode shifts the center position
        mapFragment.centerCurrentPositionIfFollowing()
    }

    private fun setIsFollowingPosition(follow: Boolean) {
        val mapFragment = mapFragment ?: return
        mapFragment.isFollowingPosition = follow
        binding.gpsTrackingButton.isActivated = follow
        if (follow) mapFragment.centerCurrentPositionIfFollowing()
    }

    /* -------------------------------------- Context Menu -------------------------------------- */

    private fun showMapContextMenu(position: LatLon) {
        val popupMenu = PopupMenu(requireContext(), binding.contextMenuView)
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
        val ctx = context ?: return

        val zoom = mapFragment?.cameraPosition?.zoom
        val uri = buildGeoUri(pos.latitude, pos.longitude, zoom)

        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (intent.resolveActivity(ctx.packageManager) != null) {
            startActivity(intent)
        } else {
            ctx.toast(R.string.map_application_missing, Toast.LENGTH_LONG)
        }
    }

    private fun onClickCreateNote(pos: LatLon) {
        if ((mapFragment?.cameraPosition?.zoom ?: 0f) < ApplicationConstants.NOTE_MIN_ZOOM) {
            context?.toast(R.string.create_new_note_unprecise)
            return
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) f.onClickClose { composeNote(pos) }
        else composeNote(pos)
    }

    private fun composeNote(pos: LatLon, hasGpxAttached: Boolean = false) {
        val mapFragment = mapFragment ?: return
        showInBottomSheet(CreateNoteFragment.create(hasGpxAttached))

        mapFragment.show3DBuildings = false
        val offsetPos = mapFragment.getPositionThatCentersPosition(pos, mapOffsetWithOpenBottomSheet)
        mapFragment.updateCameraPosition { position = offsetPos }
    }

    private fun onClickCreateTrack() {
        val mapFragment = mapFragment ?: return
        mapFragment.startPositionTrackRecording()
        binding.stopTracksButton.isVisible = true
    }

    // ---------------------------------- Location Pointer Pin  --------------------------------- */

    private fun updateLocationPointerPin() {
        val mapFragment = mapFragment ?: return
        val camera = mapFragment.cameraPosition ?: return
        val position = camera.position
        val rotation = camera.rotation

        val location = mapFragment.displayedLocation
        if (location == null) {
            binding.locationPointerPin.visibility = View.GONE
            return
        }
        val displayedPosition = LatLon(location.latitude, location.longitude)

        var target = mapFragment.getClippedPointOf(displayedPosition) ?: return
        windowInsets?.let {
            target -= PointF(it.left.toFloat(), it.top.toFloat())
        }
        val intersection = findClosestIntersection(binding.mapControls, target)
        if (intersection != null) {
            val intersectionPosition = mapFragment.getPositionAt(intersection)
            binding.locationPointerPin.isGone = intersectionPosition == null
            if (intersectionPosition != null) {
                val angleAtIntersection = position.initialBearingTo(intersectionPosition)
                binding.locationPointerPin.pinRotation = angleAtIntersection.toFloat() + (180 * rotation / PI).toFloat()

                val a = angleAtIntersection * PI / 180f + rotation
                val offsetX = (sin(a) / 2.0 + 0.5) * binding.locationPointerPin.width
                val offsetY = (-cos(a) / 2.0 + 0.5) * binding.locationPointerPin.height
                binding.locationPointerPin.x = intersection.x - offsetX.toFloat()
                binding.locationPointerPin.y = intersection.y - offsetY.toFloat()
            }
        } else {
            binding.locationPointerPin.visibility = View.GONE
        }
    }

    private fun onClickLocationPointer() {
        setIsFollowingPosition(true)
    }

    //endregion

    //region Edit History Sidebar

    private fun showEditHistorySidebar() {
        val appearAnim = R.animator.edit_history_sidebar_appear
        val disappearAnim = R.animator.edit_history_sidebar_disappear
        childFragmentManager.commit(true) {
            setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
            replace(R.id.edit_history_container, EditHistoryFragment(), EDIT_HISTORY)
            addToBackStack(EDIT_HISTORY)
        }
        mapFragment?.hideOverlay()
        mapFragment?.pinMode = MainMapFragment.PinMode.EDITS
    }

    private fun closeEditHistorySidebar() {
        if (editHistoryFragment != null) {
            childFragmentManager.popBackStack(EDIT_HISTORY, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        clearHighlighting()
        mapFragment?.clearFocus()
        mapFragment?.pinMode = MainMapFragment.PinMode.QUESTS
    }

    //endregion

    //region Bottom Sheet - Controlling the bottom sheet and its interaction with the map

    /** Close bottom sheet, clear associated highlighting on the map and return to the previous
     *  view (e.g. if it was zoomed in before to focus on an element) */
    @UiThread
    private fun closeBottomSheet() {
        activity?.currentFocus?.hideKeyboard()
        if (bottomSheetFragment != null) {
            childFragmentManager.popBackStack(BOTTOM_SHEET, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        clearHighlighting()
        unfreezeMap()
        mapFragment?.endFocus()
    }

    /** Open or replace the bottom sheet. If the bottom sheet is replaces, no appear animation is
     *  played and the highlighting of the previous bottom sheet is cleared. */
    private fun showInBottomSheet(f: Fragment, clearPreviousHighlighting: Boolean = true) {
        activity?.currentFocus?.hideKeyboard()
        freezeMap()
        if (bottomSheetFragment != null && clearPreviousHighlighting) {
            clearHighlighting()
        }
        val appearAnim = R.animator.quest_answer_form_appear
        val disappearAnim = R.animator.quest_answer_form_disappear
        childFragmentManager.commit(true) {
            setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
            replace(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET)
            addToBackStack(BOTTOM_SHEET)
        }
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
        mapFragment?.show3DBuildings = true
    }

    //endregion

    //region Bottom sheets

    @UiThread
    private fun showOverlayFormForNewElement() {
        val overlay = selectedOverlaySource.selectedOverlay ?: return
        val mapFragment = mapFragment ?: return

        val f = overlay.createForm(null) ?: return
        if (f.arguments == null) f.arguments = bundleOf()
        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        val args = AbstractOverlayForm.createArguments(overlay, null, null, rotation, tilt)
        f.requireArguments().putAll(args)

        showInBottomSheet(f)
        mapFragment.hideNonHighlightedPins()
    }

    @UiThread
    private suspend fun showElementDetails(elementKey: ElementKey) {
        if (isElementCurrentlyDisplayed(elementKey)) return
        val overlay = selectedOverlaySource.selectedOverlay ?: return
        val geometry = mapDataWithEditsSource.getGeometry(elementKey.type, elementKey.id) ?: return
        val mapFragment = mapFragment ?: return

        // open note if it is blocking element
        val center = geometry.center
        val note = withContext(Dispatchers.IO) {
            notesSource.getAll(BoundingBox(center, center).enlargedBy(1.2)).firstOrNull()
        }
        if (note != null) {
            showQuestDetails(OsmNoteQuest(note.id, note.position))
            return
        }

        val element = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(elementKey.type, elementKey.id) } ?: return
        val f = overlay.createForm(element) ?: return
        if (f.arguments == null) f.arguments = bundleOf()

        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
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
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        val args = AbstractQuestForm.createArguments(quest.key, quest.type, quest.geometry, rotation, tilt)
        f.requireArguments().putAll(args)

        if (quest is OsmQuest) {
            val element = withContext(Dispatchers.IO) { mapDataWithEditsSource.get(quest.elementType, quest.elementId) } ?: return
            val osmArgs = AbstractOsmQuestForm.createArguments(element)
            f.requireArguments().putAll(osmArgs)

            showInBottomSheet(f)
            showHighlightedElements(quest, element)
        } else {
            showInBottomSheet(f)
        }

        mapFragment.startFocus(quest.geometry, mapOffsetWithOpenBottomSheet)
        mapFragment.highlightGeometry(quest.geometry)
        mapFragment.highlightPins(quest.type.icon, quest.markerLocations)
        mapFragment.hideNonHighlightedPins()
        mapFragment.hideOverlay()
    }

    private fun showHighlightedElements(quest: OsmQuest, element: Element) {
        val bbox = quest.geometry.center.enclosingBoundingBox(quest.type.highlightedElementsRadius)
        var mapData: MapDataWithGeometry? = null

        fun getMapData(): MapDataWithGeometry {
            val data = mapDataWithEditsSource.getMapDataWithGeometry(bbox)
            mapData = data
            return data
        }

        val levels = createLevelsOrNull(element.tags)

        viewLifecycleScope.launch {
            val elements = withContext(Dispatchers.IO) {
                quest.type.getHighlightedElements(element, ::getMapData)
            }
            for (e in elements) {
                // don't highlight "this" element
                if (element == e) continue
                // include only elements with the same (=intersecting) level, if any
                val eLevels = createLevelsOrNull(e.tags)
                if (!levels.levelsIntersect(eLevels)) continue
                // include only elements with the same layer, if any
                if (element.tags["layer"] != e.tags["layer"]) continue

                val geometry = mapData?.getGeometry(e.type, e.id) ?: continue
                val icon = getPinIcon(e.tags)
                val title = getTitle(e.tags)
                putMarkerForCurrentHighlighting(geometry, icon, title)
            }
        }
    }

    private fun isQuestDetailsCurrentlyDisplayedFor(questKey: QuestKey): Boolean {
        val f = bottomSheetFragment
        return f is IsShowingQuestDetails && f.questKey == questKey
    }

    //endregion

    //region Animation - Animation(s) for when a quest is solved

    private fun showQuestSolvedAnimation(iconResId: Int, position: LatLon) {
        val ctx = context ?: return
        val offset = view?.getLocationInWindow() ?: return
        val startPos = mapFragment?.getPointOf(position) ?: return

        val size = ctx.dpToPx(42).toInt()
        startPos.x += offset.x - size / 2f
        startPos.y += offset.y - size * 1.5f

        showMarkerSolvedAnimation(iconResId, startPos)
    }

    private fun showMarkerSolvedAnimation(@DrawableRes iconResId: Int, startScreenPos: PointF) {
        val ctx = context ?: return
        val activity = activity ?: return
        val view = view ?: return

        viewLifecycleScope.launch {
            soundFx.play(resources.getIdentifier("plop" + Random.nextInt(4), "raw", ctx.packageName))
        }

        val root = activity.window.decorView as ViewGroup
        val img = EffectQuestPlopBinding.inflate(layoutInflater, root, false).root
        img.x = startScreenPos.x
        img.y = startScreenPos.y
        img.setImageResource(iconResId)
        root.addView(img)

        val answerTarget = view.findViewById<View>(
            if (isAutosync) R.id.answers_counter_fragment else R.id.upload_button_fragment
        )
        flingQuestMarkerTo(img, answerTarget) { root.removeView(img) }
    }

    private val isAutosync: Boolean get() =
        Prefs.Autosync.valueOf(prefs.getString(Prefs.AUTOSYNC, "ON")!!) == Prefs.Autosync.ON

    private fun flingQuestMarkerTo(quest: View, target: View, onFinished: () -> Unit) {
        val targetPos = target.getLocationInWindow().toPointF()
        quest.animate()
            .scaleX(1.6f).scaleY(1.6f)
            .setInterpolator(OvershootInterpolator(8f))
            .setDuration(250)
            .withEndAction {
                quest.animate()
                    .scaleX(0.2f).scaleY(0.2f)
                    .alpha(0.8f)
                    .x(targetPos.x).y(targetPos.y)
                    .setDuration(250)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction(onFinished)
            }
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++++ INTERFACE +++++++++++++++++++++++++++++++++++++++ */

    //region Interface - For the parent fragment / activity

    fun getCameraPosition(): CameraPosition? {
        return mapFragment?.cameraPosition
    }

    fun setCameraPosition(position: LatLon, zoom: Float) {
        mapFragment?.isFollowingPosition = false
        mapFragment?.isNavigationMode = false
        mapFragment?.setInitialCameraPosition(CameraPosition(position, 0f, 0f, zoom))
        setIsFollowingPosition(false)
    }

    //endregion

    companion object {
        private const val BOTTOM_SHEET = "bottom_sheet"
        private const val EDIT_HISTORY = "edit_history"

        private const val TAG_LOCATION_REQUEST = "LocationRequestFragment"
    }
}
