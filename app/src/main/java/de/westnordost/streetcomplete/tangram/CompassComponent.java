package de.westnordost.streetcomplete.tangram;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.AnyThread;
import android.view.Display;

import java.util.Timer;
import java.util.TimerTask;

/** Component that gets the sensor data from accelerometer and magnetic field, smoothens it out and
 *  makes callbacks to report a simple rotation to its parent.
 */
public class CompassComponent implements SensorEventListener
{
	private SensorManager sensorManager;
	private Display display;
	private Sensor accelerometer, magnetometer;
	private Timer compassTimer;
	private CompassAnimator compassAnimator;
	private float[] lastAccels, lastMagFields;
	/** time the compass needle needs in order to rotate into a new direction (from sensor data).
	 *  The sensor data is a bit erratic, so this smoothens it out. */
	private static final int DURATION = 200;
	// the compass doesn't move that fast, this is more than enough
	private static final int RotationUpdateFPS = 30;

	public static Location lastLocation;
	private LowPassFilter filterYaw = new LowPassFilter(0.03f);

	static float rot = (float) 0.0;
	static float tilt = (float) 0.0;

	private Listener listener;
	public interface Listener
	{
		@AnyThread void onRotationChanged(float rotation, float tilt);
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}

	public void onCreate(SensorManager sensorManager, Display display)
	{
		this.display = display;
		this.sensorManager = sensorManager;
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public void onResume()
	{
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	public void onPause()
	{
		if(compassTimer != null)
		{
			compassTimer.cancel();
			compassTimer = null;
		}
		sensorManager.unregisterListener(this);
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	private void initializeCompassAnimator()
	{
		compassTimer = new Timer();
		compassAnimator = new CompassAnimator();
		compassTimer.scheduleAtFixedRate(compassAnimator, 0, 1000/RotationUpdateFPS);
	}

	@Override public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			lastMagFields = event.values;
		}
		else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			lastAccels = event.values;
		}

		if (lastAccels != null && lastMagFields != null)
		{
			float Declination = 0;
			if(lastLocation != null) {
				GeomagneticField geoField = new GeomagneticField(
					(float) lastLocation.getLatitude(),
					(float) lastLocation.getLongitude(),
					(float) lastLocation.getAltitude(), System.currentTimeMillis());
				Declination = geoField.getDeclination();
			}

			if(compassTimer == null) {
				initializeCompassAnimator();
			}

			float rotationMatrix[] = new float[9];
			float I[] = new float[9];
			if (SensorManager.getRotationMatrix(rotationMatrix, I, lastAccels, lastMagFields)) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(rotationMatrix, orientation);
				float azimut = (float) (Math.toDegrees(orientation[0]) + Declination) ;

				rot = filterYaw.lowPass(azimut);
				compassAnimator.targetRotation = (float) Math.toRadians(rot);

				compassAnimator.targetTilt = 0;
			}
		}
	}

	/** dampens the erratic-ness of the sensors by <b>animating towards</b> the calculated rotation
	 *  and not directly setting it */
	private class CompassAnimator extends TimerTask
	{
		private final float INITIAL = -9999;
		private float currentRotation = INITIAL;
		private float currentTilt = INITIAL;
		private long lastTime = System.currentTimeMillis();


		/**
		 * 0 = north
		 * -Pi/2 = west
		 * Pi, -Pi = south
		 * Pi/2 = east
		 */
		volatile float targetRotation = INITIAL;
		volatile float targetTilt = INITIAL;

		@Override public void run()
		{
			currentRotation = animate(currentRotation, targetRotation);
			currentTilt = animate(currentTilt, targetTilt);

			if(listener != null) listener.onRotationChanged(currentRotation, currentTilt);
			lastTime = System.currentTimeMillis();
		}

		private float animate(float current, float target)
		{
			if(target == INITIAL) return current;
			if(current == INITIAL || current == target) return target;

			long deltaTime = System.currentTimeMillis() - lastTime;
			if(deltaTime > DURATION) return target;

			float deltaRotation = target - current;
			while (deltaRotation > +Math.PI) deltaRotation -= 2*Math.PI;
			while (deltaRotation < -Math.PI) deltaRotation += 2*Math.PI;

			return current + deltaRotation * deltaTime / DURATION;
		}
	}

	public class LowPassFilter {
		/*
         * time smoothing constant for low-pass filter 0 ≤ alpha ≤ 1 ; a smaller
         * value basically means more smoothing See:
         * http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
         */
		float ALPHA = 0f;
		float lastOutput = 0;

		LowPassFilter(float ALPHA) {
			this.ALPHA = ALPHA;
		}

		float lowPass(float input) {
			if (Math.abs(input - lastOutput) > 170) {
				lastOutput = input;
				return lastOutput;
			}
			lastOutput = lastOutput + ALPHA * (input - lastOutput);
			return lastOutput;
		}
	}
}
