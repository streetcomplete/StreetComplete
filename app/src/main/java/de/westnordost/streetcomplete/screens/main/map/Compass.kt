package de.westnordost.streetcomplete.screens.main.map

import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.view.Display
import android.view.Surface
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.westnordost.streetcomplete.util.ktx.nowAsEpochMilliseconds
import de.westnordost.streetcomplete.util.math.normalizeDegrees
import de.westnordost.streetcomplete.util.math.normalizeRadians
import java.lang.Math.toRadians
import kotlin.math.PI
import kotlin.math.abs

/** Component that gets the sensor data from accelerometer and magnetic field, smoothens it out and
 * makes callbacks to report a simple rotation to its parent.
 */
class Compass(
    private val sensorManager: SensorManager,
    private val display: Display,
    private val callback: (rotation: Float, tilt: Float) -> Unit,
) : SensorEventListener, DefaultLifecycleObserver {

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private var declination = 0f
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    private var rotation = 0f
    private var tilt = 0f

    private var accuracy: Int = 0

    override fun onResume(owner: LifecycleOwner) {
        accelerometer?.let { sensorManager.registerListener(this, it, 33333) }
        magnetometer?.let { sensorManager.registerListener(this, it, 33333) }
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        this.accuracy = accuracy
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (accuracy <= 0) return

        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.copyOf()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.copyOf()
        }
        val grav = gravity ?: return
        val geomag = geomagnetic ?: return

        val success = SensorManager.getRotationMatrix(rotationMatrix, null, grav, geomag)
        if (success) {
            remapToDisplayRotation(rotationMatrix)
            SensorManager.getOrientation(rotationMatrix, orientation)
            val azimuth = orientation[0] + declination
            val pitch = orientation[1]
            val roll = orientation[2]

            val r = lowPassFilterAngle(azimuth, rotation)
            val t = lowPassFilterAngle(pitch, tilt)
            if (r != rotation || t != tilt) {
                rotation = r
                tilt = t
                callback(rotation, tilt)
            }
        }
        /* reset to null. We want to do the recalculation and the callback only after an update has
           been received from both sensors */
        gravity = null
        geomagnetic = null
    }

    fun setLocation(location: Location) {
        val geomagneticField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            nowAsEpochMilliseconds()
        )
        declination = toRadians(geomagneticField.declination.toDouble()).toFloat()
    }

    private fun lowPassFilterAngle(newValue: Float, oldValue: Float): Float {
        val delta = normalizeRadians((newValue - oldValue).toDouble(), -PI).toFloat()
        if (abs(delta) <= MIN_DIFFERENCE) return oldValue
        return oldValue + SMOOTHEN_FACTOR * delta
    }

    private fun remapToDisplayRotation(r: FloatArray) {
        val h: Int
        val v: Int
        when (display.rotation) {
            Surface.ROTATION_90 -> {
                h = SensorManager.AXIS_Y
                v = SensorManager.AXIS_MINUS_X
            }
            Surface.ROTATION_180 -> {
                h = SensorManager.AXIS_MINUS_X
                v = SensorManager.AXIS_MINUS_Y
            }
            Surface.ROTATION_270 -> {
                h = SensorManager.AXIS_MINUS_Y
                v = SensorManager.AXIS_X
            }
            Surface.ROTATION_0 -> {
                h = SensorManager.AXIS_X
                v = SensorManager.AXIS_Y
            }
            else -> return
        }
        SensorManager.remapCoordinateSystem(r, h, v, r)
    }

    companion object {
        private const val SMOOTHEN_FACTOR = 0.1f
        private const val MIN_DIFFERENCE = 0.001f
    }
}
