package de.westnordost.streetcomplete.tangram;

import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.AnyThread;
import android.view.Display;
import android.view.Surface;


/** Component that gets the sensor data from accelerometer and magnetic field, smoothens it out and
 *  makes callbacks to report a simple rotation to its parent.
 */
public class CompassComponent implements SensorEventListener
{
	private static final int MAX_DISPATCH_FPS = 30;
	private static final float SMOOTHEN_FACTOR = 0.1f;
	private static final float MIN_DIFFERENCE = 0.005f;

	private SensorManager sensorManager;
	private Display display;
	private Sensor accelerometer, magnetometer;
	private float[] gravity, geomagnetic;

	private float declination;
	private float rotation, tilt;

	private Listener listener;

	private Handler sensorHandler;
	private HandlerThread sensorThread;
	private Thread dispatcherThread;

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
		sensorThread = new HandlerThread("Compass Sensor Thread");
		sensorThread.start();
		sensorHandler = new Handler(sensorThread.getLooper());
	}

	private float[] remapToDisplayRotation(float[] inR)
	{
		int h, v;
		float[] outR = new float[9];
		switch (display.getRotation()) {
			case Surface.ROTATION_90:
				h = SensorManager.AXIS_Y;
				v = SensorManager.AXIS_MINUS_X;
				break;
			case Surface.ROTATION_180:
				h = SensorManager.AXIS_MINUS_X;
				v = SensorManager.AXIS_MINUS_Y;
				break;
			case Surface.ROTATION_270:
				h = SensorManager.AXIS_MINUS_Y;
				v = SensorManager.AXIS_X;
				break;
			case Surface.ROTATION_0:
			default:
				return inR;
		}
		SensorManager.remapCoordinateSystem(inR, h, v, outR);
		return outR;
	}

	public void onResume()
	{
		sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI, sensorHandler);
		sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI, sensorHandler);
		dispatcherThread = new Thread(this::dispatchLoop, "Compass Dispatcher Thread");
		dispatcherThread.start();
	}

	public void onPause()
	{
		sensorManager.unregisterListener(this);
		dispatcherThread.interrupt();
		dispatcherThread = null;
	}

	public void onDestroy()
	{
		listener = null;
		sensorThread.quit();
	}

	@Override public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override public void onSensorChanged(SensorEvent event)
	{
		if (listener == null) return;

		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
		{
			geomagnetic = event.values.clone();
		}
		else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			gravity = event.values.clone();
		}

		if (gravity != null && geomagnetic != null)
		{
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
			if (success) {
				R = remapToDisplayRotation(R);
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				float azimut = orientation[0] - declination;
				float pitch = orientation[1];
				float roll = orientation[2];

				rotation = azimut;
				tilt = pitch;
			}
		}
	}

	private void dispatchLoop()
	{
		boolean first = true;
		float lastTilt = 0, lastRotation = 0, t = 0, r = 0;
		while(!Thread.interrupted()) {
			try	{ Thread.sleep(1000 / MAX_DISPATCH_FPS); } catch (InterruptedException e) { return; }

			if(first && (rotation != 0 || tilt != 0))
			{
				r = rotation;
				t = tilt;
				first = false;
			} else
			{
				r = smoothenAngle(rotation, r, SMOOTHEN_FACTOR);
				t = smoothenAngle(tilt, t, SMOOTHEN_FACTOR);
			}

			if(Math.abs(lastTilt - t) > MIN_DIFFERENCE || Math.abs(lastRotation - r) > MIN_DIFFERENCE)
			{
				listener.onRotationChanged(r, t);
				lastTilt = t;
				lastRotation = r;
			}
		}
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
