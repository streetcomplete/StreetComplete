package de.westnordost.streetcomplete.tangram

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.TypeEvaluator
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.animation.doOnStart
import com.mapzen.tangram.CameraPosition
import com.mapzen.tangram.CameraUpdateFactory
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapController

/** Controls the camera of a Tangram MapController. Use in place of the
 *  MapController.updateCameraPosition functions to enable parallel animations with an easy API.
 *  Usage example:
 *  <br><br>
 *  <pre>
 *  // move to new position within 500ms and at the same time zoom to level 18.5 and tilt within 1000ms
 *  cameraController.updateCamera(500) {
 *    position = LngLat(2.0, 3.0)
 *  }
 *  cameraController.updateCamera(1000) {
 *    zoom = 18.5
 *    tilt = 0.4
 *  }
 *  </pre>
 *  */
class CameraController(private val c: MapController) {
    private val defaultInterpolator = AccelerateDecelerateInterpolator()
    private val doubleTypeEvaluator = DoubleTypeEvaluator()
    private val currentAnimations = mutableMapOf<String, ObjectAnimator?>()
    private val mainHandler = Handler(Looper.getMainLooper())


    private val _camera = CameraPosition()
    val camera: CameraPosition
        get() {
            synchronized(_camera) {
                pullCameraPositionFromController()
                return _camera
            }
        }

    fun updateCamera(duration: Long = 0, interpolator: Interpolator = defaultInterpolator, builder: CameraUpdate.() -> Unit) {
        val cameraUpdate = CameraUpdate()
        cameraUpdate.builder()
        clearCameraAnimations(cameraUpdate)
        if (duration == 0L) {
            applyCameraUpdate(cameraUpdate)
        } else {
            animateCameraUpdate(cameraUpdate, duration, interpolator)
        }
    }

    private fun clearCameraAnimations(update: CameraUpdate) {
        if(update.rotation != null) {
            clearAnimation("rotation")
        }
        if(update.tilt != null) {
            clearAnimation("tilt")
        }
        if(update.zoom != null) {
            clearAnimation("rotation")
        }
        if(update.position != null) clearAnimation("rotation")
    }

    private fun applyCameraUpdate(update: CameraUpdate) {
        synchronized(_camera) {
            pullCameraPositionFromController()
            update.position?.let {
                _camera.latitude = it.latitude
                _camera.longitude = it.longitude
            }
            update.rotation?.let { _camera.rotation = it }
            update.tilt?.let { _camera.tilt = it }
            update.zoom?.let { _camera.zoom = it }
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
        animator.target = this
        animator.setValues(*propValues.toTypedArray())
        animator.duration = duration
        animator.interpolator = interpolator
        animator.addUpdateListener {
            synchronized(_camera) {
                pushCameraPositionToController()
            }
        }
        mainHandler.run { animator.start() }
    }

    private fun pullCameraPositionFromController() {
        c.getCameraPosition(_camera)
    }

    private fun pushCameraPositionToController() {
        c.updateCameraPosition(CameraUpdateFactory.newCameraPosition(_camera))
    }

    private fun clearAnimation(key: String) {
        var animator: ObjectAnimator?
        synchronized(currentAnimations) {
            animator = currentAnimations[key]
            currentAnimations[key] = null
        }
        mainHandler.run { animator?.cancel() }
    }

    private fun assignAnimation(key: String, animator: ObjectAnimator) {
        synchronized(currentAnimations) {
            currentAnimations[key] = animator
        }
    }
}

class CameraUpdate {
    var position: LngLat? = null
    var rotation: Float? = null
    var tilt: Float? = null
    var zoom: Float? = null
}

class DoubleTypeEvaluator : TypeEvaluator<Double> {
    override fun evaluate(t: Float, x0: Double, x1: Double) = x0 + t * (x1 - x0)
}
