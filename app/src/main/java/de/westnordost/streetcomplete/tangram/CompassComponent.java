package de.westnordost.streetcomplete.tangram;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.Timer;
import java.util.TimerTask;

/** Component that gets the sensor data from accelerometer and magnetic field, smoothens it out and
 *  makes callbacks to report a simple rotation to its parent.
 */
public class CompassComponent implements SensorEventListener
{
	private SensorManager sensorManager;
	private Sensor accelerometer, magnetometer;
	private Timer compassTimer;
	private CompassAnimator compassAnimator;
	private float[] gravity, geomagnetic;
	/** time the compass needle needs in order to rotate into a new direction (from sensor data).
	 *  The sensor data is a bit erratic, so this smoothens it out. */
	private static final int DURATION = 200;
	// the compass doesn't move that fast, this is more than enough
	private static final int RotationUpdateFPS = 30;

	private Listener listener;
	public interface Listener
	{
		void onRotationChanged(float rotation);
	}

	public void setListener(Listener listener)
	{
		this.listener = listener;
	}

	public void onCreate(SensorManager sensorManager)
	{
		this.sensorManager = sensorManager;
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
	}

	public void onResume()
	{
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);

		compassTimer = new Timer();
		compassAnimator = new CompassAnimator();
		compassTimer.scheduleAtFixedRate(compassAnimator, 0, 1000/RotationUpdateFPS);
	}

	public void onPause()
	{
		compassTimer.cancel();
		sensorManager.unregisterListener(this);
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override public void onSensorChanged(SensorEvent event)
	{
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			gravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			geomagnetic = event.values;
		if (gravity != null && geomagnetic != null)
		{
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimut = orientation[0]; // orientation contains: azimut, pitch and roll
				compassAnimator.targetRotation = azimut;
			}
		}
	}

	/** dampens the erratic-ness of the sensors by <b>animating towards</b> the calculated rotation
	 *  and not directly setting it */
	private class CompassAnimator extends TimerTask
	{
		private float INITIAL = -9999;
		private float currentRotation = INITIAL;
		private long lastTime = System.currentTimeMillis();

		volatile float targetRotation = INITIAL;

		@Override public void run()
		{
			if(targetRotation == INITIAL) return;
			if(currentRotation == INITIAL)
			{
				currentRotation = targetRotation;
			}
			else if(currentRotation != targetRotation)
			{
				float deltaRotation = targetRotation - currentRotation;
				while (deltaRotation > +Math.PI) deltaRotation -= 2*Math.PI;
				while (deltaRotation < -Math.PI) deltaRotation += 2*Math.PI;
				long currentTime = System.currentTimeMillis();
				long deltaTime = currentTime - lastTime;

				if(deltaTime > DURATION) currentRotation = targetRotation;
				else currentRotation += deltaRotation * deltaTime / DURATION;
			}
			if(listener != null) listener.onRotationChanged(currentRotation);
			lastTime = System.currentTimeMillis();
		}
	}
}
