package de.westnordost.streetcomplete.tangram;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.annotation.AnyThread;
import android.view.Display;
import android.view.Surface;


/** Component that gets the sensor data from accelerometer and magnetic field, smoothens it out and
 *  makes callbacks to report a simple rotation to its parent.
 */
public class CompassComponent implements SensorEventListener
{
	private static float SMOOTHEN_FACTOR = 0.1f;

	private SensorManager sensorManager;
	private Display display;
	private Sensor accelerometer, magnetometer;
	private float[] gravity, geomagnetic;

	private float declination;
	private float rotation, tilt;
	private boolean isRotationSet;

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

	private float getDisplayTilt(float pitch, float roll)
	{
		switch (display.getRotation())
		{
			case Surface.ROTATION_0: return pitch;
			case Surface.ROTATION_90: return roll;
			case Surface.ROTATION_180: return -pitch;
			case Surface.ROTATION_270: return -roll;
		}
		return 0;
	}

	private int getDisplayRotation()
	{
		switch (display.getRotation())
		{
			case Surface.ROTATION_0: return 0;
			case Surface.ROTATION_90: return 90;
			case Surface.ROTATION_180: return 180;
			case Surface.ROTATION_270: return 270;
		}
		return 0;
	}

	public void onResume()
	{
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
	}

	public void onPause()
	{
		sensorManager.unregisterListener(this);
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override public void onSensorChanged(SensorEvent event)
	{
		if (listener == null) return;

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			geomagnetic = event.values;
		}
		else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			gravity = event.values;
		}

		if (gravity != null && geomagnetic != null)
		{
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimut = orientation[0];
				float pitch = orientation[1];
				float roll = orientation[2];

				float displayRotation = (float) (Math.PI * getDisplayRotation() / 180);
				float displayTilt = getDisplayTilt(pitch, roll);

				if(!isRotationSet)
				{
					rotation = azimut;
					tilt = displayTilt;
					isRotationSet = true;
				}
				else
				{
					rotation = smoothenAngle(azimut, rotation, SMOOTHEN_FACTOR);
					tilt = smoothenAngle(displayTilt, tilt, SMOOTHEN_FACTOR);
				}
				onRotationChanged(rotation + displayRotation - declination, tilt);
			}
		}
	}

	private float lastTilt, lastRotation;
	private void onRotationChanged(float r, float t)
	{
		if(Math.abs(this.lastTilt - t) < 0.005 && Math.abs(this.lastRotation - r) < 0.005) return;
		listener.onRotationChanged(r,t);
		this.lastTilt = t;
		this.lastRotation = r;
	}

	private static float smoothenAngle(float newValue, float oldValue, float factor)
	{
		float delta = newValue - oldValue;
		while (delta > +Math.PI) delta -= 2*Math.PI;
		while (delta < -Math.PI) delta += 2*Math.PI;
		return oldValue + factor * delta;
	}

	public void setLocation(Location location)
	{
		GeomagneticField geomagneticField = new GeomagneticField(
			(float) location.getLatitude(),
			(float) location.getLongitude(),
			(float) location.getAltitude(),
			System.currentTimeMillis());
		declination = (float) Math.toRadians(geomagneticField.getDeclination());
	}
}
