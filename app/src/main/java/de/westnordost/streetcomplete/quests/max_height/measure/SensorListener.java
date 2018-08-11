package de.westnordost.streetcomplete.quests.max_height.measure;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensorListener implements SensorEventListener
{

	private final float[] accelerometerReading = new float[3];
	private final float[] magnetometerReading = new float[3];

	private final float[] rotationMatrix = new float[9];
	private final float[] orientationAngles = new float[3];

	public float getPitch()
	{
		return orientationAngles[1];
	}

	public float getPitchQuadrantUpDown()
	{
		return rotationMatrix[8];
	}

	public void updateOrientationAngles()
	{
		SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
		SensorManager.getOrientation(rotationMatrix, orientationAngles);
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
			System.arraycopy(event.values, 0, accelerometerReading,0, accelerometerReading.length);
		}
		else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			System.arraycopy(event.values, 0, magnetometerReading,0, magnetometerReading.length);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int i) {
		updateOrientationAngles();
	}
}
