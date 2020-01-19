package de.westnordost.streetcomplete.map

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PointF
import android.graphics.Rect
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mapzen.android.lost.api.LocationRequest
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Quest
import de.westnordost.streetcomplete.data.QuestGroup
import de.westnordost.streetcomplete.data.osm.ElementGeometry
import de.westnordost.streetcomplete.location.LocationRequestFragment
import de.westnordost.streetcomplete.location.LocationState
import de.westnordost.streetcomplete.location.LocationUtil
import de.westnordost.streetcomplete.location.SingleLocationRequest
import de.westnordost.streetcomplete.map.tangram.CameraPosition
import de.westnordost.streetcomplete.map.tangram.CameraUpdate
import kotlinx.android.synthetic.main.fragment_map_with_controls.*
import kotlin.math.max

/** Contains the quests map and the controls for it. */
class MainFragment : Fragment(R.layout.fragment_map_with_controls),
    MapFragment.Listener {

    private var singleLocationRequest: SingleLocationRequest? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isShowingControls = true
    private var wasFollowingPosition = false
    private var wasCompassMode = false

    private var mapFragment: QuestsMapFragment? = null

    private var listener: Listener? = null

    interface Listener {
        fun onClickCreateNote()
    }

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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        singleLocationRequest = SingleLocationRequest(context)
        listener = activity as Listener
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
            listener?.onClickCreateNote()
        }

        isShowingControls = savedInstanceState?.getBoolean(SHOW_CONTROLS) ?: true

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

    override fun onStart() {
        super.onStart()
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
        context!!.unregisterReceiver(locationAvailabilityReceiver)
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(locationRequestFinishedReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mainHandler.removeCallbacksAndMessages(null)
    }

    /* --------------------------------- MapFragment.Listener ----------------------------------- */

    override fun onMapInitialized() {
        gpsTrackingButton.isActivated = mapFragment?.isFollowingPosition ?: false
        gpsTrackingButton.isCompassMode = mapFragment?.isCompassMode ?: false
    }

    override fun onMapIsChanging() {
        val cameraPos = mapFragment?.cameraPosition ?: return
        compassNeedleView.rotation = (180 * cameraPos.rotation / Math.PI).toFloat()
        compassNeedleView.rotationX = (180 * cameraPos.tilt / Math.PI).toFloat()
    }

    override fun onPanBegin() {
        setIsFollowingPosition(false)
    }

    override fun onMapDidChange(animated: Boolean) { }

    /* --------------------------------------- Location ----------------------------------------- */

    private fun updateLocationAvailability() {
        if (LocationUtil.isLocationOn(activity)) {
            onLocationIsEnabled()
        } else {
            onLocationIsDisabled()
        }
    }

    private fun onLocationIsEnabled() {
        gpsTrackingButton.state = LocationState.SEARCHING
        mapFragment!!.startPositionTracking()
        singleLocationRequest?.startRequest(LocationRequest.PRIORITY_HIGH_ACCURACY) {
            gpsTrackingButton.state = LocationState.UPDATING
        }
    }

    private fun onLocationIsDisabled() {
        gpsTrackingButton.state = if (LocationUtil.hasLocationPermission(activity)) LocationState.ALLOWED else LocationState.DENIED
        mapFragment!!.stopPositionTracking()
        singleLocationRequest?.stopRequest()
    }

    private fun onLocationRequestFinished(state: LocationState) {
        if (activity == null) return
        gpsTrackingButton.state = state
        if (state.isEnabled) {
            updateLocationAvailability()
        }
    }

    /* --------------------------------- Map control buttons------------------------------------- */

    private fun onClickZoomOut() {
        mapFragment?.updateCameraPosition(500) { zoomBy = -1f }
    }

    private fun onClickZoomIn() {
        mapFragment?.updateCameraPosition(500) { zoomBy = +1f }
    }

    private fun onClickCompassButton() {
        val mapFragment = mapFragment ?: return
        val isNorthUp = mapFragment.cameraPosition?.rotation == 0f
        if (!isNorthUp) {
            mapFragment.updateCameraPosition {
                rotation = 0f
                tilt = 0f
            }
        }
        if (mapFragment.isFollowingPosition) {
            setIsCompassMode(!mapFragment.isCompassMode)
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

    /* ------------------------------------------------------------------------------------------ */

    fun freezeMap() {
        val mapFragment = mapFragment ?: return

        wasFollowingPosition = mapFragment.isFollowingPosition
        wasCompassMode = mapFragment.isCompassMode
        mapFragment.isFollowingPosition = false
        mapFragment.isCompassMode = false
        hideMapControls()
    }

    fun unfreezeMap() {
        val mapFragment = mapFragment ?: return

        mapFragment.isFollowingPosition = wasFollowingPosition
        mapFragment.isCompassMode = wasCompassMode
        mapFragment.endFocusQuest()
        showMapControls()
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

    /* ------------------------------------------------------------------------------------------ */

    // TODO forwarding...

    fun getCameraPosition(): CameraPosition? {
        return mapFragment?.cameraPosition
    }

    @JvmOverloads
    fun updateCameraPosition(
        duration: Long = 0,
        interpolator: Interpolator = AccelerateDecelerateInterpolator(),
        builder: (CameraUpdate) -> Unit) {
        mapFragment?.updateCameraPosition(duration, interpolator, builder)
    }

    fun putMarkerForCurrentQuest(point: LatLon) {
        mapFragment?.putMarkerForCurrentQuest(point)
    }

    fun deleteMarkerForCurrentQuest(point: LatLon) {
        mapFragment?.deleteMarkerForCurrentQuest(point)
    }

    fun setFollowingPosition(toggleOn: Boolean) {
        mapFragment?.isFollowingPosition = toggleOn
    }

    fun getDisplayedLocation(): Location? {
        return mapFragment?.displayedLocation
    }

    fun addQuestPins(quests: Iterable<Quest>, group: QuestGroup) {
        mapFragment?.addQuestPins(quests, group)
    }

    fun removeQuestPins(questIds: Collection<Long>, group: QuestGroup) {
        mapFragment?.removeQuestPins(questIds, group)
    }

    fun startFocusQuest(geometry: ElementGeometry) {
        mapFragment?.startFocusQuest(geometry)
    }

    fun getDisplayedArea(): BoundingBox? {
        return mapFragment?.getDisplayedArea()
    }

    fun setQuestOffset(rect: Rect) {
        mapFragment?.questOffset = rect
    }

    fun setShowingQuestPins(toggleOn: Boolean) {
        mapFragment?.isShowingQuestPins = toggleOn
    }

    fun getPositionAt(point: PointF): LatLon? {
        return mapFragment?.getPositionAt(point)
    }

    fun getPointOf(pos: LatLon): PointF? {
        return mapFragment?.getPointOf(pos)
    }

    companion object {
        private const val SHOW_CONTROLS = "ShowControls"
    }
}
