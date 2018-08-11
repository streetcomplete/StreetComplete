package de.westnordost.streetcomplete.quests.max_height.measure;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

import de.westnordost.streetcomplete.FragmentContainerActivity;
import de.westnordost.streetcomplete.R;

public class MeasureCameraActivity extends FragmentContainerActivity implements MeasureListener
{
	public static final String
		UNIT = "unit",
		METERS = "meters",
		FEET = "feet",
		INCHES = "inches";

	@Override public void onMeasured(String meters)
	{
		Intent intent = new Intent();
		intent.putExtra(UNIT, METERS);
		intent.putExtra(METERS, meters);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override public void onMeasured(String feet, String inches)
	{
		Intent intent = new Intent();
		intent.putExtra(UNIT, FEET);
		intent.putExtra(FEET, feet);
		intent.putExtra(INCHES, inches);
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	@Override public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_height_measure_camera, menu);
		return true;
	}
}
