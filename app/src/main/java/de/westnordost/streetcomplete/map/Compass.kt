package de.westnordost.streetcomplete.map

import android.hardware.*
import android.location.Location
import android.os.Handler
import android.os.HandlerThread
import android.view.Display
import android.view.Surface
import java.lang.Math.toRadians
import kotlin.math.PI
import kotlin.math.abs

/** Component that gets the sensor data from accelerometer and magnetic field, smoothens it out and
 * makes callbacks to report a simple rotation to its parent.
 */
class Compass(
    private val sensorManager: SensorManager,
    private val display: Display,
    private val callback: (rotation: Float, tilt: Float) -> Unit
) : SensorEventListener {

    private val accelerometer: Sensor?
    private val magnetometer: Sensor?

    private val sensorThread: HandlerThread
    private val sensorHandler: Handler

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    private var declination = 0f
    private var rotation = 0f
    private var tilt = 0f

    private var dispatcherThread: Thread? = null

    init {
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorThread = HandlerThread("Compass Sensor Thread")
        sensorThread.start()
        sensorHandler = Handler(sensorThread.looper)
    }

    private fun remapToDisplayRotation(inR: FloatArray): FloatArray {
        val h: Int
        val v: Int
        val outR = FloatArray(9)
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
            Surface.ROTATION_0 -> return inR
            else -> return inR
        }
        SensorManager.remapCoordinateSystem(inR, h, v, outR)
        return outR
    }

    fun onResume() {
        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI, sensorHandler) }
        magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI, sensorHandler) }

        dispatcherThread = Thread(Runnable { dispatchLoop() }, "Compass Dispatcher Thread")
        dispatcherThread?.start()
    }

    fun onPause() {
        sensorManager.unregisterListener(this)

        dispatcherThread?.interrupt()
        dispatcherThread = null
    }

    fun onDestroy() {
        sensorHandler.removeCallbacksAndMessages(null)
        sensorThread.quit()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone()
        }
        val grav = gravity ?: return
        val geomag = geomagnetic ?: return

        var R = FloatArray(9)
        val I = FloatArray(9)
        val success = SensorManager.getRotationMatrix(R, I, grav, geomag)
        if (success) {
            R = remapToDisplayRotation(R)
            val orientation = FloatArray(3)
            SensorManager.getOrientation(R, orientation)
            val azimut = orientation[0] - declination
            val pitch = orientation[1]
            val roll = orientation[2]
            rotation = azimut
            tilt = pitch
        }
    }

    private fun dispatchLoop() {
        var first = true
        var lastTilt = 0f
        var lastRotation = 0f
        var t = 0f
        var r = 0f
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(1000 / MAX_DISPATCH_FPS.toLong())
            } catch (e: InterruptedException) {
                return
            }
            if (first && (rotation != 0f || tilt != 0f)) {
                r = rotation
                t = tilt
                first = false
            } else {
                r = smoothenAngle(rotation, r, SMOOTHEN_FACTOR)
                t = smoothenAngle(tilt, t, SMOOTHEN_FACTOR)
            }
            if (abs(lastTilt - t) > MIN_DIFFERENCE || abs(lastRotation - r) > MIN_DIFFERENCE) {
                callback(r, t)
                lastTilt = t
                lastRotation = r
            }
        }
    }

    fun setLocation(location: Location) {
        val geomagneticField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = toRadians(geomagneticField.declination.toDouble()).toFloat()
    }

    private fun smoothenAngle( newValue: Float, oldValue: Float, factor: Float): Float {
        var delta = newValue - oldValue
        while (delta > +PI) delta -= 2 * PI.toFloat()
        while (delta < -PI) delta += 2 * PI.toFloat()
        return oldValue + factor * delta
    }

    companion object {
        private const val MAX_DISPATCH_FPS = 30
        private const val SMOOTHEN_FACTOR = 0.1f
        private const val MIN_DIFFERENCE = 0.005f
    }
}

