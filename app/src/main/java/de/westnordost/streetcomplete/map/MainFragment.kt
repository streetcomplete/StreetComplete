package de.westnordost.streetcomplete.map

import android.annotation.SuppressLint
import android.content.*
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.location.Location
import android.location.LocationManager
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
import androidx.core.content.getSystemService
import androidx.core.graphics.Insets
import androidx.core.graphics.minus
import androidx.core.graphics.toPointF
import androidx.core.graphics.toRectF
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.streetcomplete.*
import de.westnordost.streetcomplete.controls.MainMenuButtonFragment
import de.westnordost.streetcomplete.controls.UndoButtonFragment
import de.westnordost.streetcomplete.data.edithistory.Edit
import de.westnordost.streetcomplete.data.edithistory.EditKey
import de.westnordost.streetcomplete.data.osm.edits.split_way.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osm.geometry.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.BoundingBox
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.data.osm.osmquests.OsmQuest
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.databinding.EffectQuestPlopBinding
import de.westnordost.streetcomplete.databinding.FragmentMainBinding
import de.westnordost.streetcomplete.edithistory.EditHistoryFragment
import de.westnordost.streetcomplete.ktx.*
import de.westnordost.streetcomplete.location.createLocationAvailabilityIntentFilter
import de.westnordost.streetcomplete.location.FineLocationManager
import de.westnordost.streetcomplete.location.hasLocationPermission
import de.westnordost.streetcomplete.location.isLocationEnabled
import de.westnordost.streetcomplete.location.LocationRequestFragment
import de.westnordost.streetcomplete.location.LocationState
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.quests.*
import de.westnordost.streetcomplete.util.*
import de.westnordost.streetcomplete.view.insets_animation.respectSystemInsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.*
import kotlin.random.Random

/** Contains the quests map and the controls for it.
 *
 *  This fragment controls the main view. It does itself not contain that much logic, but it is the
 *  place where all the logic when interacting with the map / bottom sheets / sidebars etc. comes
 *  together, hence it implements all the listeners to communicate with its child fragments.
 *  */
class MainFragment : Fragment(R.layout.fragment_main),
    MapFragment.Listener,
    LocationAwareMapFragment.Listener,
    QuestsMapFragment.Listener,
    AbstractQuestAnswerFragment.Listener,
    SplitWayFragment.Listener,
    LeaveNoteInsteadFragment.Listener,
    CreateNoteFragment.Listener,
    VisibleQuestsSource.Listener,
    MainMenuButtonFragment.Listener,
    UndoButtonFragment.Listener,
    EditHistoryFragment.Listener,
    HandlesOnBackPressed {

    @Inject internal lateinit var questController: QuestController
    @Inject internal lateinit var isSurveyChecker: QuestSourceIsSurveyChecker
    @Inject internal lateinit var visibleQuestsSource: VisibleQuestsSource
    @Inject internal lateinit var soundFx: SoundFx
    @Inject internal lateinit var prefs: SharedPreferences

    private lateinit var locationManager: FineLocationManager

    private val binding by viewBinding(FragmentMainBinding::bind)

    private var wasFollowingPosition = true
    private var wasNavigationMode = false

    private var locationWhenOpenedQuest: Location? = null

    private var windowInsets: Insets? = null

    internal var mapFragment: QuestsMapFragment? = null
    internal var mainMenuButtonFragment: MainMenuButtonFragment? = null

    private val bottomSheetFragment: Fragment? get() =
        childFragmentManagerOrNull?.findFragmentByTag(BOTTOM_SHEET)

    private val editHistoryFragment: EditHistoryFragment? get() =
        childFragmentManagerOrNull?.findFragmentByTag(EDIT_HISTORY) as? EditHistoryFragment

    private var mapOffsetWithOpenBottomSheet: RectF = RectF(0f, 0f, 0f, 0f)

    interface Listener {
        fun onQuestSolved(quest: Quest, source: String?)
        fun onCreatedNote(screenPosition: Point)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    /* +++++++++++++++++++++++++++++++++++++++ CALLBACKS ++++++++++++++++++++++++++++++++++++++++ */

    //region Lifecycle - Android Lifecycle Callbacks

    private val locationAvailabilityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateLocationAvailability()
        }
    }

    private val locationRequestFinishedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = LocationState.valueOf(intent.getStringExtra(LocationRequestFragment.STATE)!!)
            onLocationRequestFinished(state)
        }
    }

    init {
        Injector.applicationComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        locationManager = FineLocationManager(context.getSystemService<LocationManager>()!!, this::onLocationChanged)

        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            when (fragment) {
                is QuestsMapFragment -> mapFragment = fragment
                is MainMenuButtonFragment -> mainMenuButtonFragment = fragment
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapControls.respectSystemInsets(View::setMargins)
        view.respectSystemInsets { windowInsets = it }

        binding.locationPointerPin.setOnClickListener { onClickLocationPointer() }

        binding.compassView.setOnClickListener { onClickCompassButton() }
        binding.gpsTrackingButton.setOnClickListener { onClickTrackingButton() }
        binding.zoomInButton.setOnClickListener { onClickZoomIn() }
        binding.zoomOutButton.setOnClickListener { onClickZoomOut() }

        updateMapQuestOffsets()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mapFragment = this.mapFragment ?: return
        /* when rotating the screen and the bottom sheet is open, the view
           should not rotate around its proper center but around the center
           of the part of the map that is not occluded by the bottom sheet */
        val previousOffset = mapOffsetWithOpenBottomSheet
        updateMapQuestOffsets()
        if (bottomSheetFragment != null) {
            mapFragment.adjustToOffsets(previousOffset, mapOffsetWithOpenBottomSheet)
        }
        updateLocationPointerPin()
    }

    override fun onStart() {
        super.onStart()
        visibleQuestsSource.addListener(this)
        requireContext().registerReceiver(
            locationAvailabilityReceiver,
            createLocationAvailabilityIntentFilter()
        )
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            locationRequestFinishedReceiver,
            IntentFilter(LocationRequestFragment.ACTION_FINISHED)
        )
        updateLocationAvailability()
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
        wasFollowingPosition = mapFragment?.isFollowingPosition ?: true
        wasNavigationMode = mapFragment?.isNavigationMode ?: false
        visibleQuestsSource.removeListener(this)
        requireContext().unregisterReceiver(locationAvailabilityReceiver)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationRequestFinishedReceiver)
        locationManager.removeUpdates()
    }

    private fun updateMapQuestOffsets() {
        mapOffsetWithOpenBottomSheet = Rect(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            0,
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        ).toRectF()
    }

    //endregion

    //region QuestsMapFragment - Callbacks from the map with its quest pins

    /* ---------------------------------- MapFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        val isFollowingPosition = mapFragment?.isFollowingPosition ?: false
        binding.gpsTrackingButton.isActivated = isFollowingPosition
        updateLocationPointerPin()
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        binding.compassView.rotation = (180 * rotation / PI).toFloat()
        binding.compassView.rotationX = (180 * tilt / PI).toFloat()

        val margin = 2 * PI / 180
        binding.compassView.isInvisible = abs(rotation) < margin && tilt < margin

        updateLocationPointerPin()

        val f = bottomSheetFragment
        if (f is AbstractQuestAnswerFragment<*>) f.onMapOrientation(rotation, tilt)
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
        if (f is IsCloseableBottomSheet) f.onClickClose {
            viewLifecycleScope.launch { showQuestDetails(questKey) }
        }
        else viewLifecycleScope.launch { showQuestDetails(questKey) }
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
        } else if(editHistoryFragment != null) {
            closeEditHistorySidebar()
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

    /* -------------------------- AbstractQuestAnswerFragment.Listener -------------------------- */

    override fun onAnsweredQuest(questKey: QuestKey, answer: Any) {
        viewLifecycleScope.launch {
            solveQuest(questKey) { quest ->
                if (questController.solve(quest, answer, "survey")) {
                    onQuestSolved(quest, "survey")
                }
            }
        }
    }

    override fun onComposeNote(questKey: QuestKey, questTitle: String) {
        showInBottomSheet(LeaveNoteInsteadFragment.create(questKey, questTitle))
    }

    override fun onSplitWay(osmQuestKey: OsmQuestKey) {
        viewLifecycleScope.launch {
            val quest = questController.get(osmQuestKey)!!
            val element = questController.getOsmElement(quest as OsmQuest)
            val geometry = quest.geometry
            if (element is Way && geometry is ElementPolylinesGeometry) {
                mapFragment?.pinMode = QuestsMapFragment.PinMode.NONE
                mapFragment?.highlightElement(geometry)
                showInBottomSheet(SplitWayFragment.create(osmQuestKey, element, geometry))
            }
        }
    }

    override fun onSkippedQuest(questKey: QuestKey) {
        viewLifecycleScope.launch {
            closeBottomSheet()
            questController.hide(questKey)
        }
    }

    override fun onDeletePoiNode(osmQuestKey: OsmQuestKey) {
        viewLifecycleScope.launch {
            solveQuest(osmQuestKey) { quest ->
                if (questController.deletePoiElement(quest as OsmQuest, "survey")) {
                    onQuestSolved(quest, "survey")
                }
            }
        }
    }

    override fun onReplaceShopElement(osmQuestKey: OsmQuestKey, tags: Map<String, String>) {
        viewLifecycleScope.launch {
            solveQuest(osmQuestKey) { quest ->
                if (questController.replaceShopElement(quest as OsmQuest, tags, "survey")) {
                    onQuestSolved(quest, "survey")
                }
            }
        }
    }

    private suspend fun solveQuest(questKey: QuestKey, onIsSurvey: suspend (quest: Quest) -> Unit) {
        val f = (bottomSheetFragment as? IsLockable)
        f?.locked = true
        val quest = questController.get(questKey) ?: return
        val ctx = context ?: return

        val checkLocations = listOfNotNull(mapFragment?.displayedLocation, locationWhenOpenedQuest)
        if (isSurveyChecker.checkIsSurvey(ctx, quest.geometry, checkLocations)) {
            closeBottomSheet()
            onIsSurvey(quest)
        } else {
            f?.locked = false
        }
    }

    private fun onQuestSolved(quest: Quest, source: String?) {
        listener?.onQuestSolved(quest, source)
        showQuestSolvedAnimation(quest)
    }

    /* ------------------------------- SplitWayFragment.Listener -------------------------------- */

    override fun onSplittedWay(osmQuestKey: OsmQuestKey, splits: List<SplitPolylineAtPosition>) {
        viewLifecycleScope.launch {
            solveQuest(osmQuestKey) { quest ->
                if (questController.splitWay(quest as OsmQuest, splits, "survey")) {
                    onQuestSolved(quest, "survey")
                }
            }
        }
    }

    override fun onAddSplit(point: LatLon) {
        mapFragment?.putMarkerForCurrentQuest(point, R.drawable.crosshair_marker)
    }

    override fun onRemoveSplit(point: LatLon) {
        mapFragment?.deleteMarkerForCurrentQuest(point)
    }

    /* --------------------------- LeaveNoteInsteadFragment.Listener ---------------------------- */

    override fun onCreatedNoteInstead(questKey: QuestKey, questTitle: String, note: String, imagePaths: List<String>) {
        // the quest is deleted from DB on creating a note, so need to fetch quest before
        viewLifecycleScope.launch {
            val quest = questController.get(questKey)
            if (quest != null) {
                closeBottomSheet()
                if (questController.createNote(questKey, questTitle, note, imagePaths)) {
                    onQuestSolved(quest, null)
                }
            }
        }
    }

    /* ------------------------------- CreateNoteFragment.Listener ------------------------------ */

    override fun onCreatedNote(note: String, imagePaths: List<String>, screenPosition: Point) {
        val mapFragment = mapFragment ?: return
        val mapView = mapFragment.view ?: return
        if (!mapFragment.isMapInitialized) return

        val mapPosition = mapView.getLocationInWindow().toPointF()
        val notePosition = screenPosition.toPointF()
        notePosition.offset(-mapPosition.x, -mapPosition.y)
        val position = mapFragment.getPositionAt(notePosition) ?: throw NullPointerException()

        /* closing the bottom sheet must happen after getting the position because on closing the
           bottom sheet, the view will immediately follow the user's location again (if that setting
           is on) #3284
         */
        closeBottomSheet()

        viewLifecycleScope.launch { questController.createNote(note, imagePaths, position) }

        listener?.onCreatedNote(screenPosition)
        showMarkerSolvedAnimation(R.drawable.ic_quest_create_note, PointF(screenPosition))
    }

    //endregion

    //region Data Updates - Callbacks for when data changed in the local database

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread override fun onUpdatedVisibleQuests(added: Collection<Quest>, removed: Collection<QuestKey>) {
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            if (f !is IsShowingQuestDetails) return@launch

            // open quest does not exist anymore!
            if (removed.contains(f.questKey)) {
                closeBottomSheet()
            }
        }
    }

    @AnyThread override fun onVisibleQuestsInvalidated() {
        viewLifecycleScope.launch {
            val f = bottomSheetFragment
            if (f !is IsShowingQuestDetails) return@launch

            val openQuest = withContext(Dispatchers.IO) { questController.get(f.questKey) }
            if (openQuest == null) {
                closeBottomSheet()
            }
        }
    }

    //endregion

    //region Edit History - Callbacks from the Edit History Sidebar

    override fun onSelectedEdit(edit: Edit) {
        mapFragment?.startFocusEdit(edit, mapOffsetWithOpenBottomSheet)
    }

    override fun onDeletedSelectedEdit() {
        mapFragment?.endFocusEdit()
    }

    override fun onEditHistoryIsEmpty() {
        closeEditHistorySidebar()
    }

    //endregion

    /* ++++++++++++++++++++++++++++++++++++++ VIEW CONTROL ++++++++++++++++++++++++++++++++++++++ */

    //region Location - Request location and update location status

    private fun updateLocationAvailability() {
        if (requireContext().isLocationEnabled) {
            onLocationIsEnabled()
        } else {
            onLocationIsDisabled()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationIsEnabled() {
        binding.gpsTrackingButton.state = LocationState.SEARCHING
        mapFragment!!.startPositionTracking()

        setIsFollowingPosition(wasFollowingPosition)
        locationManager.requestSingleUpdate()
    }

    private fun onLocationIsDisabled() {
        binding.gpsTrackingButton.state = if (requireContext().hasLocationPermission)
            LocationState.ALLOWED else LocationState.DENIED
        binding.locationPointerPin.visibility = View.GONE
        mapFragment!!.clearPositionTracking()
        locationManager.removeUpdates()
    }

    private fun onLocationRequestFinished(state: LocationState) {
        if (activity == null) return
        binding.gpsTrackingButton.state = state
        if (state.isEnabled) {
            updateLocationAvailability()
        }
    }

    private fun onLocationChanged(location: Location) {
        binding.gpsTrackingButton.state = LocationState.UPDATING
        updateLocationPointerPin()
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
                val tag = LocationRequestFragment::class.java.simpleName
                val locationRequestFragment = activity?.supportFragmentManager?.findFragmentByTag(tag) as LocationRequestFragment?
                locationRequestFragment?.startRequest()
            }
            !mapFragment.isFollowingPosition -> {
                setIsFollowingPosition(true)
            }
            else -> {
                setIsNavigationMode(!mapFragment.isNavigationMode)
            }
        }
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
            when(item.itemId) {
                R.id.action_create_note -> onClickCreateNote(position)
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
        if (mapFragment?.cameraPosition?.zoom ?: 0f < ApplicationConstants.NOTE_MIN_ZOOM) {
            context?.toast(R.string.create_new_note_unprecise)
            return
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) f.onClickClose { composeNote(pos) }
        else composeNote(pos)
    }

    private fun composeNote(pos: LatLon) {
        val mapFragment = mapFragment ?: return
        mapFragment.show3DBuildings = false
        val offsetPos = mapFragment.getPositionThatCentersPosition(pos, mapOffsetWithOpenBottomSheet)
        mapFragment.updateCameraPosition { position = offsetPos }

        freezeMap()
        showInBottomSheet(CreateNoteFragment())
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
        mapFragment?.pinMode = QuestsMapFragment.PinMode.EDITS
    }

    private fun closeEditHistorySidebar() {
        if (editHistoryFragment != null) {
            childFragmentManager.popBackStack(EDIT_HISTORY, POP_BACK_STACK_INCLUSIVE)
        }
        mapFragment?.pinMode = QuestsMapFragment.PinMode.QUESTS
    }

    //endregion

    //region Bottom Sheet - Controlling and managing the bottom sheet contents

    @UiThread private fun closeBottomSheet() {
        activity?.currentFocus?.hideKeyboard()
        if (bottomSheetFragment != null) {
            childFragmentManager.popBackStack(BOTTOM_SHEET, POP_BACK_STACK_INCLUSIVE)
        }
        unfreezeMap()
    }

    private suspend fun showQuestDetails(questKey: QuestKey) {
        val quest = questController.get(questKey)
        if (quest != null) {
            showQuestDetails(quest)
        }
    }

    @UiThread private suspend fun showQuestDetails(quest: Quest) {
        val mapFragment = mapFragment ?: return
        if (isQuestDetailsCurrentlyDisplayedFor(quest.key)) return
        if (bottomSheetFragment != null) {
            activity?.currentFocus?.hideKeyboard()
            if (bottomSheetFragment != null) {
                childFragmentManager.popBackStack(BOTTOM_SHEET, POP_BACK_STACK_INCLUSIVE)
            }
            resetFreezeMap()
            mapFragment.startFocusQuest(quest, mapOffsetWithOpenBottomSheet)
        } else {
            mapFragment.startFocusQuest(quest, mapOffsetWithOpenBottomSheet)
            freezeMap()
            locationWhenOpenedQuest = mapFragment.displayedLocation
        }

        val f = quest.type.createForm()
        val element = if (quest is OsmQuest) {
            questController.getOsmElement(quest) ?: return
        } else {
            null
        }
        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        val args = AbstractQuestAnswerFragment.createArguments(quest, element, rotation, tilt)
        if(f.arguments != null) {
            f.arguments!!.putAll(args)
        } else {
            f.arguments = args
        }
        showInBottomSheet(f)
    }

    private fun showInBottomSheet(f: Fragment) {
        val appearAnim = if (bottomSheetFragment == null) R.animator.quest_answer_form_appear else 0
        val disappearAnim = R.animator.quest_answer_form_disappear
        childFragmentManager.commit(true) {
            setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
            replace(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET)
            addToBackStack(BOTTOM_SHEET)
        }
    }

    private fun isQuestDetailsCurrentlyDisplayedFor(questKey: QuestKey): Boolean {
        val f = bottomSheetFragment
        return f is IsShowingQuestDetails && f.questKey == questKey
    }

    private fun freezeMap() {
        val mapFragment = mapFragment ?: return

        wasFollowingPosition = mapFragment.isFollowingPosition
        wasNavigationMode = mapFragment.isNavigationMode
        mapFragment.isFollowingPosition = false
        mapFragment.isNavigationMode = false
    }

    private fun resetFreezeMap() {
        val mapFragment = mapFragment ?: return

        mapFragment.clearFocusQuest()
        mapFragment.show3DBuildings = true
        mapFragment.pinMode = QuestsMapFragment.PinMode.QUESTS
    }

    private fun unfreezeMap() {
        val mapFragment = mapFragment ?: return

        mapFragment.isFollowingPosition = wasFollowingPosition
        mapFragment.isNavigationMode = wasNavigationMode
        mapFragment.endFocusQuest()
        mapFragment.show3DBuildings = true
        mapFragment.pinMode = QuestsMapFragment.PinMode.QUESTS
    }

    //endregion

    //region Animation - Animation(s) for when a quest is solved

    private fun showQuestSolvedAnimation(quest: Quest) {
        val ctx = context ?: return
        val offset = view?.getLocationInWindow() ?: return
        val startPos = mapFragment?.getPointOf(quest.position) ?: return

        val size = 42f.toPx(ctx).toInt()
        startPos.x += offset.x - size / 2f
        startPos.y += offset.y - size * 1.5f

        showMarkerSolvedAnimation(quest.type.icon, startPos)
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
            if (isAutosync) R.id.answers_counter_fragment else R.id.upload_button_fragment)
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
    }
}
