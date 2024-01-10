package de.westnordost.streetcomplete.screens.main.map.tangram

import android.animation.Animator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.content.ContentResolver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import de.westnordost.streetcomplete.data.maptiles.toLatLng
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.ktx.runImmediate
import kotlin.math.PI

/**
 *  Controls the camera of a Tangram MapController. Use in place of the
 *  MapController.updateCameraPosition methods to enable parallel animations with an easy API.
 *  Usage example:
 *
 *  // move to new position within 500ms and at the same time zoom to level 18.5 and tilt within 1000ms
 *  cameraManager.updateCamera(500) {
 *    position = LngLat(2.0, 3.0)
 *  }
 *  cameraManager.updateCamera(1000) {
 *    zoom = 18.5
 *    tilt = 0.4
 *  }
 *
 *  See https://github.com/tangrams/tangram-es/issues/1962
 *  */
class CameraManager(private val mapboxMap: MapboxMap, private val contentResolver: ContentResolver) {
    private val defaultInterpolator = AccelerateDecelerateInterpolator()
    private val doubleTypeEvaluator = DoubleTypeEvaluator()
    private val currentAnimations = mutableMapOf<String, Animator>()
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastAnimator: ValueAnimator? = null
        set(value) {
            if (field == value) return
            mainHandler.post {
                if (field == null) {
                    listener?.onAnimationsStarted()
                } else if (value == null) {
                    listener?.onAnimationsEnded()
                }
            }
            field = value
        }
    private var lastAnimatorEndTime: Long = 0

    private val _mablibreCamera = com.mapbox.mapboxsdk.camera.CameraPosition.Builder().build() // todo: can't be changed
    val camera: CameraPosition get() = CameraPosition(mapboxMap.cameraPosition)

    val isAnimating: Boolean get() = lastAnimator != null

    var maximumTilt: Float = PI.toFloat() / 6f // 60° // todo: use it

    interface AnimationsListener {
        @UiThread fun onAnimationsStarted()
        @UiThread fun onAnimating()
        @UiThread fun onAnimationsEnded()
    }
    var listener: AnimationsListener? = null

    @AnyThread fun updateCamera(duration: Long = 0, update: CameraUpdate) {
        synchronized(mapboxMap) { // todo: where to synchronize?
            update.resolveDeltas(_mablibreCamera)
            if (duration == 0L || isAnimationsOff) {
                applyCameraUpdate(update) // todo: mapLibre camera should be set here, because applyCameraUpdate is also called at the end of animateCameraUpdate
            } else {
                val cameraPositionBuilder = com.mapbox.mapboxsdk.camera.CameraPosition.Builder(mapboxMap.cameraPosition)
                update.rotation?.let { cameraPositionBuilder.bearing(it) }
                update.position?.let { cameraPositionBuilder.target(it.toLatLng()) }
                update.zoom?.let { cameraPositionBuilder.zoom(it) }
                update.tilt?.let { cameraPositionBuilder.tilt(it) }
                mapboxMap.easeCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build()), duration.toInt())
            }
        }
    }

    @AnyThread fun cancelAllCameraAnimations() {
        mainHandler.runImmediate {
            synchronized(currentAnimations) {
                for (animator in currentAnimations.values.toSet()) {
                    animator.cancel()
                }
                currentAnimations.clear()
            }
        }
    }

    private val isAnimationsOff get() =
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

    private fun applyCameraUpdate(update: CameraUpdate) {
        val cameraPositionBuilder = com.mapbox.mapboxsdk.camera.CameraPosition.Builder(mapboxMap.cameraPosition)
        update.position?.let {
            cameraPositionBuilder.target(LatLng(it.latitude, it.longitude))
        }
        update.rotation?.let {
            cameraPositionBuilder.bearing(it)
        }
        update.tilt?.let {
            cameraPositionBuilder.tilt(it)
        }
        update.zoom?.let {
            cameraPositionBuilder.zoom(it)
        }
        mapboxMap.moveCamera(com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newCameraPosition(cameraPositionBuilder.build()))
    }
}

class CameraUpdate {
    var position: LatLon? = null
    var rotation: Double? = null
    var tilt: Double? = null
    var zoom: Double? = null

    var zoomBy: Double? = null
    var tiltBy: Double? = null
    var rotationBy: Double? = null
}

typealias MaplibreCameraPosition = com.mapbox.mapboxsdk.camera.CameraPosition

data class CameraPosition(
    val position: LatLon,
    val rotation: Double,
    val tilt: Double,
    val zoom: Double
) {

    constructor(p: MaplibreCameraPosition) : this(p.target?.toLatLon() ?: LatLon(0.0, 0.0), p.bearing, p.tilt, p.zoom)
}

fun LatLng.toLatLon() = LatLon(latitude, longitude)

private fun CameraUpdate.resolveDeltas(pos: MaplibreCameraPosition) {
    zoomBy?.let { zoom = pos.zoom + (zoom ?: 0.0) + it }
    tiltBy?.let { tilt = pos.tilt + (tilt ?: 0.0) + it }
    rotationBy?.let { rotation = pos.bearing + (rotation ?: 0.0) + it }
}

class DoubleTypeEvaluator : TypeEvaluator<Double> {
    override fun evaluate(t: Float, x0: Double, x1: Double) = x0 + t * (x1 - x0)
}
