package de.westnordost.streetcomplete.map

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import androidx.core.graphics.toPointF
import androidx.core.graphics.toRectF
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.FragmentTransaction
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.ApplicationConstants
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Quest
import de.westnordost.streetcomplete.data.QuestController
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.VisibleQuestListener
import de.westnordost.streetcomplete.data.osm.ElementPolylinesGeometry
import de.westnordost.streetcomplete.data.osm.OsmQuest
import de.westnordost.streetcomplete.data.osm.changes.SplitPolylineAtPosition
import de.westnordost.streetcomplete.data.osmnotes.CreateNoteFragment
import de.westnordost.streetcomplete.ktx.getLocationInWindow
import de.westnordost.streetcomplete.ktx.toast
import de.westnordost.streetcomplete.location.*
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.quests.*
import kotlinx.android.synthetic.main.fragment_map_with_controls.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.PI

/** Contains the quests map and the controls for it. */
class MainFragment : Fragment(R.layout.fragment_map_with_controls),
    MapFragment.Listener, QuestsMapFragment.Listener, AbstractQuestAnswerFragment.Listener,
    SplitWayFragment.Listener, LeaveNoteInsteadFragment.Listener, CreateNoteFragment.Listener,
    VisibleQuestListener {

    @Inject internal lateinit var questController: QuestController
    @Inject internal lateinit var isSurveyChecker: QuestSourceIsSurveyChecker

    private lateinit var locationManager: FineLocationManager
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isShowingControls = true
    private var wasFollowingPosition = false
    private var wasCompassMode = false

    private var locationWhenOpenedQuest: Location? = null

    private var mapFragment: QuestsMapFragment? = null
    private val bottomSheetFragment: Fragment? get() = childFragmentManager.findFragmentByTag(BOTTOM_SHEET)

    private var mapOffsetWithOpenBottomSheet: RectF = RectF(0f, 0f, 0f, 0f)

    interface Listener {
        fun onQuestSolved(quest: Quest?, source: String?)
        fun onCreatedNote(screenPosition: Point)
    }
    private val listener: Listener? get() = parentFragment as? Listener ?: activity as? Listener

    private val locationAvailabilityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateLocationAvailability()
        }
    }

    private val locationRequestFinishedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = LocationState.valueOf(intent.getStringExtra(LocationRequestFragment.STATE))
            onLocationRequestFinished(state)
        }
    }

    /* --------------------------------------- Lifecycle ---------------------------------------- */

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        locationManager = FineLocationManager(
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager,
            this::onLocationChanged
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compassView.setOnClickListener { onClickCompassButton() }
        gpsTrackingButton.setOnClickListener { onClickTrackingButton() }
        zoomInButton.setOnClickListener { onClickZoomIn() }
        zoomOutButton.setOnClickListener { onClickZoomOut() }
        createNoteButton.setOnClickListener { v ->
            v.isEnabled = false
            mainHandler.postDelayed({ v.isEnabled = true }, 200)
            onClickCreateNote()
        }

        isShowingControls = savedInstanceState?.getBoolean(SHOW_CONTROLS) ?: true

        updateMapQuestOffsets()

        view.doOnLayout {
            if (!isShowingControls) {
                hideAll(leftSideContainer, -1)
                hideAll(rightSideContainer, +1)
            }
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is QuestsMapFragment) {
            mapFragment = childFragment
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mapFragment = mapFragment ?: return
        /* when rotating the screen and the bottom sheet is open, the view should not rotate around
           its proper center but around the center of the part of the map that is not occluded by
           the bottom sheet */
        if (bottomSheetFragment != null) {
            val currentPos = mapFragment.getViewPosition(mapOffsetWithOpenBottomSheet)
            updateMapQuestOffsets()
            if (currentPos != null) {
                val offsetPos = mapFragment.getPositionThatCentersPosition(currentPos, mapOffsetWithOpenBottomSheet)
                mapFragment.updateCameraPosition { position = offsetPos }
            }
        } else {
            updateMapQuestOffsets()
        }
    }

    override fun onStart() {
        super.onStart()
        questController.addListener(this)
        context!!.registerReceiver(
            locationAvailabilityReceiver,
            LocationUtil.createLocationAvailabilityIntentFilter()
        )
        LocalBroadcastManager.getInstance(context!!).registerReceiver(
            locationRequestFinishedReceiver,
            IntentFilter(LocationRequestFragment.ACTION_FINISHED)
        )
        updateLocationAvailability()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SHOW_CONTROLS, isShowingControls)
    }

    override fun onStop() {
        super.onStop()
        questController.removeListener(this)
        context!!.unregisterReceiver(locationAvailabilityReceiver)
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(locationRequestFinishedReceiver)
        locationManager.removeUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }

    /* ---------------------------------- MapFragment.Listener ---------------------------------- */

    override fun onMapInitialized() {
        gpsTrackingButton.isActivated = mapFragment?.isFollowingPosition ?: false
        gpsTrackingButton.isCompassMode = mapFragment?.isCompassMode ?: false
    }

    override fun onMapIsChanging(position: LatLon, rotation: Float, tilt: Float, zoom: Float) {
        compassNeedleView.rotation = (180 * rotation / Math.PI).toFloat()
        compassNeedleView.rotationX = (180 * tilt / Math.PI).toFloat()

        val f = bottomSheetFragment
        if (f is AbstractQuestAnswerFragment<*>) f.onMapOrientation(rotation, tilt)
    }

    override fun onPanBegin() {
        setIsFollowingPosition(false)
    }

    override fun onMapDidChange(position: LatLon, rotation: Float, tilt: Float, zoom: Float, animated: Boolean) { }

    /* ------------------------------- QuestsMapFragment.Listener ------------------------------- */

    override fun onClickedQuest(questGroup: QuestGroup, questId: Long) {
        if (isQuestDetailsCurrentlyDisplayedFor(questId, questGroup)) return
        val retrieveQuest: () -> Unit = {
            val quest = questController.get(questId, questGroup)
            if (quest != null) {
                showQuestDetails(quest, questGroup)
            }
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) f.onClickClose(retrieveQuest)
        else retrieveQuest()
    }

    override fun onClickedMapAt(position: LatLon, clickAreaSizeInMeters: Double) {
        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) {
            if (!f.onClickMapAt(position, clickAreaSizeInMeters))
                f.onClickClose { closeBottomSheet() }
        }
    }

    /* -------------------------- AbstractQuestAnswerFragment.Listener -------------------------- */

    override fun onAnsweredQuest(questId: Long, group: QuestGroup, answer: Any) {
        val ctx = context ?: return

        val checkLocations = listOfNotNull(mapFragment?.displayedLocation, locationWhenOpenedQuest)

        isSurveyChecker.assureIsSurvey(ctx, questId, group, checkLocations) {
            val quest = questController.get(questId, group)
            closeQuestDetailsFor(questId, group)
            if (questController.solve(questId, group, answer, "survey")) {
                listener?.onQuestSolved(quest, "survey")
            }
        }
    }

    override fun onComposeNote(questId: Long, group: QuestGroup, questTitle: String) {
        showInBottomSheet(LeaveNoteInsteadFragment.create(questId, group, questTitle))
    }

    override fun onSplitWay(osmQuestId: Long) {
        val quest = questController.get(osmQuestId, QuestGroup.OSM)!!
        val element = questController.getOsmElement(quest as OsmQuest)
        if (element !is Way) return
        val geometry = quest.geometry
        if (geometry !is ElementPolylinesGeometry) return
        mapFragment?.isShowingQuestPins = false
        showInBottomSheet(SplitWayFragment.create(osmQuestId, element, geometry))
    }

    override fun onSkippedQuest(questId: Long, group: QuestGroup) {
        closeQuestDetailsFor(questId, group)
        questController.hide(questId, group)
    }

    /* ------------------------------- SplitWayFragment.Listener -------------------------------- */

    override fun onSplittedWay(osmQuestId: Long, splits: List<SplitPolylineAtPosition>) {
        val ctx = context ?: return

        val checkLocations = listOfNotNull(mapFragment?.displayedLocation, locationWhenOpenedQuest)
        isSurveyChecker.assureIsSurvey(ctx, osmQuestId, QuestGroup.OSM, checkLocations) {
            val quest = questController.get(osmQuestId, QuestGroup.OSM)
            closeQuestDetailsFor(osmQuestId, QuestGroup.OSM)
            if (questController.splitWay(osmQuestId, splits, "survey")) {
                listener?.onQuestSolved(quest, "survey")
            }
        }
    }

    override fun onAddSplit(point: LatLon) {
        mapFragment?.putMarkerForCurrentQuest(point)
    }

    override fun onRemoveSplit(point: LatLon) {
        mapFragment?.deleteMarkerForCurrentQuest(point)
    }

    /* --------------------------- LeaveNoteInsteadFragment.Listener ---------------------------- */

    override fun onCreatedNoteInstead(questId: Long, group: QuestGroup, questTitle: String, note: String, imagePaths: List<String>?) {
        closeQuestDetailsFor(questId, group)
        // the quest is deleted from DB on creating a note, so need to fetch quest before
        val quest = questController.get(questId, group)
        if (questController.createNote(questId, questTitle, note, imagePaths)) {
            listener?.onQuestSolved(quest, null)
        }
    }

    /* ------------------------------ CreateNoteFragment.Listener ------------------------------- */

    override fun onCreatedNote(note: String, imagePaths: List<String>?, screenPosition: Point) {
        closeBottomSheet()

        val mapFragment = mapFragment ?: return
        val mapView = mapFragment.view ?: return

        val mapPosition = mapView.getLocationInWindow().toPointF()
        val notePosition = PointF(screenPosition)
        notePosition.offset(-mapPosition.x, -mapPosition.y)
        val position = mapFragment.getPositionAt(notePosition) ?: throw NullPointerException()

        questController.createNote(note, imagePaths, position)

        listener?.onCreatedNote(screenPosition)
    }

    /* ---------------------------------- VisibleQuestListener ---------------------------------- */

    @AnyThread override fun onQuestsCreated(quests: Collection<Quest>, group: QuestGroup) {
        // to recreate element geometry of selected quest (if any) after recreation of activity
        val f = bottomSheetFragment
        if (f !is IsShowingQuestDetails) return
        if (group != f.questGroup) return
        val quest = quests.find { it.id == f.questId } ?: return

        mainHandler.post { showQuestDetails(quest, group) }
    }

    @AnyThread override fun onQuestsRemoved(questIds: Collection<Long>, group: QuestGroup) {
        val f = bottomSheetFragment
        if (f !is IsShowingQuestDetails) return
        if (group != f.questGroup) return
        if (!questIds.contains(f.questId)) return

        mainHandler.post { closeBottomSheet() }
    }

    /* --------------------------------------- Location ----------------------------------------- */

    private fun updateLocationAvailability() {
        if (LocationUtil.isLocationOn(activity)) {
            onLocationIsEnabled()
        } else {
            onLocationIsDisabled()
        }
    }

    @SuppressLint("MissingPermission")
    private fun onLocationIsEnabled() {
        gpsTrackingButton.state = LocationState.SEARCHING
        mapFragment!!.startPositionTracking()
        locationManager.requestSingleUpdate()
    }

    private fun onLocationIsDisabled() {
        gpsTrackingButton.state = if (LocationUtil.hasLocationPermission(activity)) LocationState.ALLOWED else LocationState.DENIED
        mapFragment!!.stopPositionTracking()
        locationManager.removeUpdates()
    }

    private fun onLocationRequestFinished(state: LocationState) {
        if (activity == null) return
        gpsTrackingButton.state = state
        if (state.isEnabled) {
            updateLocationAvailability()
        }
    }

    private fun onLocationChanged(location: Location) {
        gpsTrackingButton?.state = LocationState.UPDATING
    }

    /* --------------------------------- Map control buttons------------------------------------- */

    private fun onClickCreateNote() {
        if (mapFragment?.cameraPosition?.zoom ?: 0f < ApplicationConstants.NOTE_MIN_ZOOM) {
            context?.toast(R.string.create_new_note_unprecise)
            return
        }

        val f = bottomSheetFragment
        if (f is IsCloseableBottomSheet) f.onClickClose { composeNote() }
        else composeNote()
    }

    private fun onClickZoomOut() {
        mapFragment?.updateCameraPosition(300) { zoomBy = -1f }
    }

    private fun onClickZoomIn() {
        mapFragment?.updateCameraPosition(300) { zoomBy = +1f }
    }

    private fun onClickCompassButton() {
        val mapFragment = mapFragment ?: return
        // Allow a small margin of error around north/flat. This both matches
        // UX expectations ("it looks straight..") and works around a bug where
        // the rotation/tilt are not set to perfectly 0 during animation
        val margin = 0.025f // About 4%
        // 2PI radians = full circle of rotation = also north
        val isNorthUp = mapFragment.cameraPosition?.rotation?.let {
            it <= margin || 2f*PI.toFloat()-it <= margin
        } ?: false
        // Camera cannot rotate upside down => full circle check not needed
        val isFlat = mapFragment.cameraPosition?.tilt?.let { it <= margin } ?: false
        if (!isNorthUp) {
            mapFragment.updateCameraPosition(300) {
                rotation = 0f
                tilt = 0f
            }
        }
        if (mapFragment.isFollowingPosition) {
            setIsCompassMode(!mapFragment.isCompassMode)
        } else {
            if (isNorthUp) {
                mapFragment.updateCameraPosition(300) {
                    tilt = if (isFlat) PI.toFloat() / 5f else 0f
                }
            }
        }
    }

    private fun onClickTrackingButton() {
        val mapFragment = mapFragment ?: return
        if (gpsTrackingButton.state.isEnabled) {
            setIsFollowingPosition(!mapFragment.isFollowingPosition)
        } else {
            val tag = LocationRequestFragment::class.java.simpleName
            val locationRequestFragment = activity!!.supportFragmentManager.findFragmentByTag(tag) as LocationRequestFragment?
            locationRequestFragment?.startRequest()
        }
    }

    private fun composeNote() {
        val mapFragment = mapFragment ?: return
        mapFragment.show3DBuildings = false
        focusOnCurrentLocationIfInView()
        freezeMap()
        showInBottomSheet(CreateNoteFragment())
    }

    private fun focusOnCurrentLocationIfInView() {
        val mapFragment = mapFragment ?: return
        val location = mapFragment.displayedLocation
        if (location != null) {
            val displayedPosition = OsmLatLon(location.latitude, location.longitude)
            if (mapFragment.isPositionInView(displayedPosition)) {
                val offsetPos = mapFragment.getPositionThatCentersPosition(displayedPosition, mapOffsetWithOpenBottomSheet)
                mapFragment.updateCameraPosition { position = offsetPos }
            }
        }
    }

    private fun setIsFollowingPosition(follow: Boolean) {
        val mapFragment = mapFragment ?: return
        mapFragment.isFollowingPosition = follow
        gpsTrackingButton.isActivated = follow
        if (!follow) setIsCompassMode(false)
    }


    private fun setIsCompassMode(compassMode: Boolean) {
        val mapFragment = mapFragment ?: return
        mapFragment.isCompassMode = compassMode
        gpsTrackingButton.isCompassMode = compassMode
    }

    /* --------------------------------- Managing bottom sheet  --------------------------------- */

    /** Called by the activity when the user presses the back button.
     *  Returns true if the event should be consumed. */
    fun onBackPressed(): Boolean {
        val f = bottomSheetFragment
        if (f !is IsCloseableBottomSheet) return false

        f.onClickClose { closeBottomSheet() }
        return true
    }

    @UiThread private fun closeBottomSheet() {
        // manually close the keyboard before popping the fragment
        val view: View? = activity?.currentFocus
        if (view != null) {
            val inputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
        }
        childFragmentManager.popBackStackImmediate(BOTTOM_SHEET, POP_BACK_STACK_INCLUSIVE)
        unfreezeMap()
    }

    @UiThread private fun showQuestDetails(quest: Quest, group: QuestGroup) {
        val mapFragment = mapFragment ?: return
        if (isQuestDetailsCurrentlyDisplayedFor(quest.id!!, group)) return
        if (bottomSheetFragment != null) {
            closeBottomSheet()
        }

        mapFragment.startFocusQuest(quest.geometry, mapOffsetWithOpenBottomSheet)
        freezeMap()
        locationWhenOpenedQuest = mapFragment.displayedLocation

        val f = quest.type.createForm()
        val element = if (quest is OsmQuest) questController.getOsmElement(quest) else null
        val camera = mapFragment.cameraPosition
        val rotation = camera?.rotation ?: 0f
        val tilt = camera?.tilt ?: 0f
        f.arguments = AbstractQuestAnswerFragment.createArguments(quest, group, element, rotation, tilt)
        showInBottomSheet(f)
    }

    private fun showInBottomSheet(f: Fragment) {
        val appearAnim = if (bottomSheetFragment == null) R.animator.quest_answer_form_appear else 0
        val disappearAnim = R.animator.quest_answer_form_disappear
        val ft: FragmentTransaction = childFragmentManager.beginTransaction()
        ft.setCustomAnimations(appearAnim, disappearAnim, appearAnim, disappearAnim)
        ft.replace(R.id.map_bottom_sheet_container, f, BOTTOM_SHEET)
        ft.addToBackStack(BOTTOM_SHEET)
        ft.commit()
    }

    private fun closeQuestDetailsFor(questId: Long, group: QuestGroup) {
        if (isQuestDetailsCurrentlyDisplayedFor(questId, group)) {
            closeBottomSheet()
        }
    }

    private fun isQuestDetailsCurrentlyDisplayedFor(questId: Long, group: QuestGroup): Boolean {
        val f = bottomSheetFragment
        return f is IsShowingQuestDetails && f.questId == questId && f.questGroup == group
    }

    /* ------------------------------------------------------------------------------------------ */

    private fun freezeMap() {
        val mapFragment = mapFragment ?: return

        wasFollowingPosition = mapFragment.isFollowingPosition
        wasCompassMode = mapFragment.isCompassMode
        mapFragment.isFollowingPosition = false
        mapFragment.isCompassMode = false
        hideMapControls()
    }

    private fun unfreezeMap() {
        val mapFragment = mapFragment ?: return

        mapFragment.isFollowingPosition = wasFollowingPosition
        mapFragment.isCompassMode = wasCompassMode
        mapFragment.endFocusQuest()
        showMapControls()
        mapFragment.show3DBuildings = true
        mapFragment.isShowingQuestPins = true
    }

    private fun hideMapControls() {
        isShowingControls = false
        animateAll(rightSideContainer, +1, false, 120, 200)
        animateAll(leftSideContainer, -1, false, 120, 200)
    }

    private fun showMapControls() {
        isShowingControls = true
        animateAll(rightSideContainer, 0, true, 120, 200)
        animateAll(leftSideContainer, 0, true, 120, 200)
    }

    private fun animateAll(parent: ViewGroup, dir: Int, animateIn: Boolean, minDuration: Int, maxDuration: Int) {
        val childCount = parent.childCount
        val w = parent.width
        for (i in 0 until childCount) {
            val v = parent.getChildAt(i)
            val order = if (animateIn) childCount - 1 - i else i
            val duration = minDuration + (maxDuration - minDuration) / max(1, childCount - 1) * order
            val animator = v.animate().translationX(w * dir.toFloat())
            animator.duration = duration.toLong()
            animator.interpolator = if (dir != 0) AccelerateInterpolator() else DecelerateInterpolator()
        }
    }

    private fun hideAll(parent: ViewGroup, dir: Int) {
        val w = parent.width
        for (i in 0 until parent.childCount) {
            val v = parent.getChildAt(i)
            v.translationX = w * dir.toFloat()
        }
    }

    private fun updateMapQuestOffsets() {
        mapOffsetWithOpenBottomSheet = Rect(
            resources.getDimensionPixelSize(R.dimen.quest_form_leftOffset),
            0,
            resources.getDimensionPixelSize(R.dimen.quest_form_rightOffset),
            resources.getDimensionPixelSize(R.dimen.quest_form_bottomOffset)
        ).toRectF()
    }


    /* ------------------------------------------------------------------------------------------ */

    fun getCameraPosition(): CameraPosition? {
        return mapFragment?.cameraPosition
    }

    fun getDisplayedArea(): BoundingBox? {
        return mapFragment?.getDisplayedArea()
    }

    fun setCameraPosition(position: LatLon, zoom: Float) {
        mapFragment?.isFollowingPosition = false
        mapFragment?.isCompassMode = false
        mapFragment?.setInitialCameraPosition(CameraPosition(position, 0f, 0f, zoom))
    }

    fun getPointOf(pos: LatLon): PointF? {
        return mapFragment?.getPointOf(pos)
    }

    companion object {
        private const val SHOW_CONTROLS = "ShowControls"
        private const val BOTTOM_SHEET = "bottom_sheet"
    }
}
