package de.westnordost.streetcomplete.map.tangram

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.TypeEvaluator
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import com.mapzen.tangram.CameraUpdateFactory
import com.mapzen.tangram.MapController
import de.westnordost.osmapi.map.data.LatLon
import de.westnordost.osmapi.map.data.OsmLatLon

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
class CameraManager(private val c: MapController) {
    private val defaultInterpolator = AccelerateDecelerateInterpolator()
    private val doubleTypeEvaluator = DoubleTypeEvaluator()
    private val currentAnimations = mutableMapOf<String, ObjectAnimator>()
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _tangramCamera = com.mapzen.tangram.CameraPosition()
    val camera: CameraPosition
        get() {
            synchronized(_tangramCamera) {
                pullCameraPositionFromController()
                return CameraPosition(_tangramCamera)
            }
        }

    fun updateCamera(duration: Long = 0, interpolator: Interpolator = defaultInterpolator, builder: (CameraUpdate) -> Unit) {
        val cameraUpdate = CameraUpdate()
        builder(cameraUpdate)
        synchronized(_tangramCamera) {
            pullCameraPositionFromController()
            cameraUpdate.resolveDeltas(_tangramCamera)
            cancelCameraAnimations(cameraUpdate)
            if (duration == 0L) {
                applyCameraUpdate(cameraUpdate)
            } else {
                animateCameraUpdate(cameraUpdate, duration, interpolator)
            }
        }
    }

    fun cancelAllCameraAnimations() {
        synchronized(currentAnimations) {
            for (animator in currentAnimations.values.toSet()) {
                animator.cancel()
            }
            currentAnimations.clear()
        }
    }

    private fun cancelCameraAnimations(update: CameraUpdate) {
        if(update.rotation != null) cancelAnimation("rotation")
        if(update.tilt != null) cancelAnimation("tilt")
        if(update.zoom != null) cancelAnimation("rotation")
        if(update.position != null) cancelAnimation("rotation")
    }

    private fun applyCameraUpdate(update: CameraUpdate) {
        synchronized(_tangramCamera) {
            pullCameraPositionFromController()
            update.position?.let {
                _tangramCamera.latitude = it.latitude
                _tangramCamera.longitude = it.longitude
            }
            update.rotation?.let { _tangramCamera.rotation = it }
            update.tilt?.let { _tangramCamera.tilt = it }
            update.zoom?.let { _tangramCamera.zoom = it }
            pushCameraPositionToController()
        }
    }

    private fun animateCameraUpdate(update: CameraUpdate, duration: Long, interpolator: Interpolator) {
        val animator = ObjectAnimator()
        val propValues = mutableListOf<PropertyValuesHolder>()
        update.rotation?.let {
            propValues.add(PropertyValuesHolder.ofFloat("rotation", it))
            assignAnimation("rotation", animator)
        }
        update.tilt?.let {
            propValues.add(PropertyValuesHolder.ofFloat("tilt", it))
            assignAnimation("tilt", animator)
        }
        update.zoom?.let {
            propValues.add(PropertyValuesHolder.ofFloat("zoom", it))
            assignAnimation("zoom", animator)
        }
        update.position?.let {
            propValues.add(PropertyValuesHolder.ofObject("latitude", doubleTypeEvaluator))
            propValues.add(PropertyValuesHolder.ofObject("longitude", doubleTypeEvaluator))
            assignAnimation("position", animator)
        }
        animator.target = _tangramCamera
        animator.setValues(*propValues.toTypedArray())
        animator.duration = duration
        animator.interpolator = interpolator
        animator.addUpdateListener {
            synchronized(_tangramCamera) {
                pushCameraPositionToController()
            }
        }
        mainHandler.run { animator.start() }
    }

    private fun pullCameraPositionFromController() {
        c.getCameraPosition(_tangramCamera)
    }

    private fun pushCameraPositionToController() {
        c.updateCameraPosition(CameraUpdateFactory.newCameraPosition(_tangramCamera))
    }

    private fun cancelAnimation(key: String) {
        var animator: ObjectAnimator?
        synchronized(currentAnimations) {
            animator = currentAnimations[key]
            if (animator != null) {
                currentAnimations.entries.removeAll { (_, anim) -> animator == anim }
            }
        }
        mainHandler.run { animator?.cancel() }
    }

    private fun assignAnimation(key: String, animator: ObjectAnimator) {
        synchronized(currentAnimations) {
            currentAnimations[key] = animator
        }
    }
}

data class CameraPosition(
    val position: LatLon,
    val rotation: Float,
    val tilt: Float,
    val zoom: Float) {

    constructor(p: com.mapzen.tangram.CameraPosition)
            : this(OsmLatLon(p.latitude, p.longitude), p.rotation, p.tilt, p.zoom)
}

class CameraUpdate {
    var position: LatLon? = null
    var rotation: Float? = null
    var tilt: Float? = null
    var zoom: Float? = null

    var zoomBy: Float? = null
    var tiltBy: Float? = null
    var rotationBy: Float? = null
}

private fun CameraUpdate.resolveDeltas(pos: com.mapzen.tangram.CameraPosition) {
    zoomBy?.let { zoom = pos.zoom + (zoom ?: 0f) + it }
    tiltBy?.let { tilt = pos.tilt + (tilt ?: 0f) + it }
    rotationBy?.let { rotation = pos.rotation + (rotation ?: 0f) + it }
}

class DoubleTypeEvaluator : TypeEvaluator<Double> {
    override fun evaluate(t: Float, x0: Double, x1: Double) = x0 + t * (x1 - x0)
}
