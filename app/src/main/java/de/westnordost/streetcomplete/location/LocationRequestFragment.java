package de.westnordost.streetcomplete.location;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import de.westnordost.streetcomplete.R;


/** Manages the process to ensure that the app can access the user's location. Two steps:
 *  <ol>
 *      <li>ask for permission</li>
 *      <li>ask for location to be turned on</li>
 *  </ol>
 *
 *  This fragment reports back to the Activity it is attached to via LocationRequestListener.
 *  The process is started via {@link #startRequest()} */
public class LocationRequestFragment extends Fragment
{
	public static final String
			ACTION_FINISHED = "de.westnordost.LocationRequestFragment.FINISHED",
			STATE = "state";

	private static final int LOCATION_PERMISSION_REQUEST = 1;
	private static final int LOCATION_TURN_ON_REQUEST = 2;

	private LocationState state;
	private boolean inProgress;
	private BroadcastReceiver locationProviderChangedReceiver;

	public LocationRequestFragment()
	{
		super();
		state = null;
	}

	/** Start location request process. When already started, will not be started again. */
	public void startRequest()
	{
		if(!inProgress)
		{
			inProgress = true;
			state = null;
			nextStep();
		}
	}

	private void nextStep()
	{
		if(state == null || state == LocationState.DENIED)
		{
			requestLocationPermissions();
		}
		else if(state == LocationState.ALLOWED)
		{
			requestLocationSettingsToBeOn();
		}
		else if(state == LocationState.ENABLED)
		{
			finish();
		}
	}

	private void finish()
	{
		inProgress = false;
		Intent intent = new Intent(ACTION_FINISHED);
		intent.putExtra(STATE, state.name());
		LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
										   @NonNull int[] grantResults)
	{
		// must be for someone else...
		if(requestCode != LOCATION_PERMISSION_REQUEST) return;
		if(permissions.length == 0 || !permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION))
			return;

		if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			requestLocationPermissions(); // retry then...
		}
		else
		{
			new AlertDialog.Builder(getContext())
					.setMessage(R.string.no_location_permission_warning)
					.setPositiveButton(R.string.retry,	(dialog, which) -> requestLocationPermissions())
					.setNegativeButton(android.R.string.cancel, (dialog, which) -> deniedlocationPermissions())
					.setOnCancelListener(dialog -> deniedlocationPermissions())
					.show();
		}
	}

	@Override public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		// must be for someone else...
		if(requestCode != LOCATION_TURN_ON_REQUEST) return;
		// we ignore the resultCode, because we always get Activity.RESULT_CANCELED. Instead, we
		// check if the conditions are fulfilled now
		requestLocationSettingsToBeOn();
	}

	private void deniedlocationPermissions()
	{
		state = LocationState.DENIED;
		finish();
	}

	private void requestLocationPermissions()
	{
		if (LocationUtil.hasLocationPermission(getContext()))
		{
			state = LocationState.ALLOWED;
			nextStep();

		} else {
			requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
					LOCATION_PERMISSION_REQUEST);
		}
	}

	private void requestLocationSettingsToBeOn()
	{
		if(LocationUtil.isLocationOn(getContext()))
		{
			state = LocationState.ENABLED;
			nextStep();
		}
		else
		{
			final AlertDialog dlg = new AlertDialog.Builder(getContext())
					.setMessage(R.string.turn_on_location_request)
					.setPositiveButton(android.R.string.yes, (dialog, which) ->
					{
						dialog.dismiss();
						Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
						startActivityForResult(viewIntent, LOCATION_TURN_ON_REQUEST);
					})
					.setNegativeButton(android.R.string.no,	(dialog, which) -> cancelTurnLocationOnDialog())
					.setOnCancelListener(dialog -> cancelTurnLocationOnDialog())
					.create();

			// the user may turn on location in the pull-down-overlay, without actually going into
			// settings dialog
			registerForLocationProviderChanges(dlg);

			dlg.show();
		}
	}

	private void cancelTurnLocationOnDialog()
	{
		unregisterForLocationProviderChanges();
		finish();
	}

	private void registerForLocationProviderChanges(final AlertDialog dlg)
	{
		locationProviderChangedReceiver = new BroadcastReceiver()
		{
			@Override public void onReceive(Context context, Intent intent)
			{
				dlg.dismiss();
				unregisterForLocationProviderChanges();
				requestLocationSettingsToBeOn();
			}
		};

		getActivity().registerReceiver(locationProviderChangedReceiver, LocationUtil.createLocationAvailabilityIntentFilter());
	}

	private void unregisterForLocationProviderChanges()
	{
		if(locationProviderChangedReceiver != null)
		{
			getActivity().unregisterReceiver(locationProviderChangedReceiver);
			locationProviderChangedReceiver = null;
		}
	}

	@Override public void onStop()
	{
		super.onStop();
		unregisterForLocationProviderChanges();
	}

	public LocationState getState()
	{
		return state != null ? state : LocationState.DENIED;
	}

	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		if(state != null) outState.putString("locationState", state.name());
		outState.putBoolean("inProgress", inProgress);
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		if(savedInstanceState != null)
		{
			String stateName = savedInstanceState.getString("locationState");
			if(stateName != null) state = LocationState.valueOf(stateName);
			inProgress = savedInstanceState.getBoolean("inProgress");
		}
	}
}
