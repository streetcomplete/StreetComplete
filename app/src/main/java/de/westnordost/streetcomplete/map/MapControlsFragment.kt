package de.westnordost.streetcomplete.map

import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.annotation.AnyThread
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mapzen.android.lost.api.LocationRequest
import de.westnordost.streetcomplete.Injector
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.location.*
import kotlinx.android.synthetic.main.fragment_map_controls.*
import kotlinx.android.synthetic.main.quest_generic_list.*
import javax.inject.Inject

// TODO this..

class MapControlsFragment : Fragment(R.layout.fragment_map_controls) {

    private var singleLocationRequest: SingleLocationRequest? = null

    private var isShowingControls = true

    private val mainHandler = Handler(Looper.getMainLooper())

    @Inject internal lateinit var prefs: SharedPreferences

    private var listener: Listener? = null

    interface Listener {
        fun onClickCreateNote()
        fun onClickZoomIn()
        fun onClickZoomOut()
        fun onClickNorthUp()
        fun onClickEnableCompassMode(enable: Boolean)

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

    /* ------------------------------------ Lifecycle ------------------------------------------- */

    init {
        Injector.instance.applicationComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listener = parentFragment as Listener
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        singleLocationRequest = SingleLocationRequest(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        compassView.setOnClickListener {
            val isFollowing = mapFragment!!.isFollowingPosition
            val isCompassMode = mapFragment!!.isCompassMode
            val isNorthUp = mapFragment!!.cameraPosition!!.rotation == 0f
            if (!isNorthUp) {
                mapFragment!!.updateCameraPosition {
                    it.rotation = 0f
                    it.tilt = 0f
                }
            }
            if (isFollowing) {
                setIsCompassMode(!isCompassMode)
            }
        }

        gpsTrackingButton.setOnClickListener {
            val state = gpsTrackingButton.state
            if (state.isEnabled) {
                if (!mapFragment!!.isFollowingPosition) {
                    setIsFollowingPosition(true)
                } else {
                    setIsFollowingPosition(false)
                }
            } else {
                val tag = LocationRequestFragment::class.java.simpleName
                val locationRequestFragment = activity!!.supportFragmentManager.findFragmentByTag(tag) as LocationRequestFragment?
                locationRequestFragment?.startRequest()
            }
        }

        zoomInButton.setOnClickListener { listener?.onClickZoomIn() }

        zoomOutButton.setOnClickListener { listener?.onClickZoomOut() }

        createNoteButton.setOnClickListener { v ->
            v.isEnabled = false
            mainHandler.postDelayed({ v.isEnabled = true }, 200)
            listener?.onClickCreateNote()
        }

        if (savedInstanceState != null) {
            isShowingControls = savedInstanceState.getBoolean(SHOW_CONTROLS)
        }

        view.doOnLayout {
            if (!isShowingControls) {
                hideAll(leftSideContainer, -1)
                hideAll(rightSideContainer, +1)
            }
        }

        mapFragment.onMapControlsCreated(this)
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

    /* ------------------------------- Calls from the MapFragment ------------------------------- */

    @AnyThread
    fun onMapOrientation(rotation: Float, tilt: Float) {
        activity!!.runOnUiThread {
            compassNeedleView!!.rotation = (180 * rotation / Math.PI).toFloat()
            compassNeedleView!!.rotationX = (180 * tilt / Math.PI).toFloat()
        }
    }

    fun onMapInitialized() {
        gpsTrackingButton.isActivated = mapFragment!!.isFollowingPosition
        gpsTrackingButton!!.isCompassMode = mapFragment!!.isCompassMode
    }

    fun hideControls() {
        isShowingControls = false
        animateAll(rightSideContainer, +1, false, 120, 200)
        animateAll(leftSideContainer, -1, false, 120, 200)
    }

    fun showControls() {
        isShowingControls = true
        animateAll(rightSideContainer, 0, true, 120, 200)
        animateAll(leftSideContainer, 0, true, 120, 200)
    }

    private fun hideAll(parent: ViewGroup?, dir: Int) {
        val w = parent!!.width
        for (i in 0 until parent.childCount) {
            val v = parent.getChildAt(i)
            v.translationX = w * dir.toFloat()
        }
    }

    private fun animateAll(parent: ViewGroup, dir: Int, `in`: Boolean, minDuration: Int, maxDuration: Int) {
        val childCount = parent.childCount
        val w = parent.width
        for (i in 0 until childCount) {
            val v = parent.getChildAt(i)
            val duration =
                minDuration + (maxDuration - minDuration) / Math.max(1, childCount - 1) *
                        if (`in`) childCount - 1 - i else i
            val animator = v.animate().translationX(w * dir.toFloat())
            animator.duration = duration.toLong()
            animator.interpolator =
                if (dir != 0) AccelerateInterpolator() else DecelerateInterpolator()
        }
    }

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

    private fun setIsFollowingPosition(follow: Boolean) {
        gpsTrackingButton.isActivated = follow
        mapFragment!!.isFollowingPosition = follow
        if (!follow) setIsCompassMode(false)
    }


    private fun setIsCompassMode(compassMode: Boolean) {
        gpsTrackingButton.isCompassMode = compassMode
        mapFragment!!.isCompassMode = compassMode
    }

    private fun onLocationRequestFinished(state: LocationState) {
        if (activity == null) return
        gpsTrackingButton.state = state
        if (state.isEnabled) {
            updateLocationAvailability()
        }
    }

    companion object {
        private const val SHOW_CONTROLS = "ShowControls"
    }
}
