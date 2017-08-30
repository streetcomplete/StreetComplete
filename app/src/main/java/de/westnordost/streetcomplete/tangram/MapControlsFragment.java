package de.westnordost.streetcomplete.tangram;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;

import com.mapzen.android.lost.api.LocationRequest;

import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationStateButton;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.location.SingleLocationRequest;
import de.westnordost.streetcomplete.view.CompassView;

import static android.location.LocationManager.PROVIDERS_CHANGED_ACTION;
import static de.westnordost.streetcomplete.location.LocationUtil.MODE_CHANGED;

public class MapControlsFragment extends Fragment
{
	private SingleLocationRequest singleLocationRequest;
	private MapFragment mapFragment;
	private CompassView compassNeedle;
	private LocationStateButton trackingButton;

	private BroadcastReceiver locationAvailabilityReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			updateLocationAvailability();
		}
	};

	private final BroadcastReceiver locationRequestFinishedReceiver = new BroadcastReceiver()
	{
		@Override public void onReceive(Context context, Intent intent)
		{
			LocationState state = LocationState.valueOf(intent.getStringExtra(LocationRequestFragment.STATE));
			onLocationRequestFinished(state);
		}
	};

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.map_controls, container, false);
		compassNeedle = view.findViewById(R.id.compassNeedle);

		view.findViewById(R.id.compass).setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				boolean isFollowing = mapFragment.isFollowingPosition();
				boolean isCompassMode = mapFragment.isCompassMode();
				boolean isNorthUp = mapFragment.getRotation() == 0;

				if(!isNorthUp)
				{
					mapFragment.setMapOrientation(0, 0);
				}

				if(isFollowing)
				{
					setIsCompassMode(!isCompassMode);
				}
			}
		});

		trackingButton = view.findViewById(R.id.gps_tracking);
		trackingButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				LocationState state = trackingButton.getState();
				if(state.isEnabled())
				{
					if(!mapFragment.isFollowingPosition())
					{
						setIsFollowingPosition(true);
					}
					else
					{
						setIsFollowingPosition(false);
					}
				}
				else
				{
					String tag = LocationRequestFragment.class.getSimpleName();
					LocationRequestFragment locationRequestFragment = (LocationRequestFragment)
							getActivity().getSupportFragmentManager().findFragmentByTag(tag);
					if(locationRequestFragment != null)
					{
						locationRequestFragment.startRequest();
					}
				}
			}
		});

		ImageButton zoomInButton = view.findViewById(R.id.zoom_in);
		zoomInButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				mapFragment.zoomIn();
			}
		});
		ImageButton zoomOutButton = view.findViewById(R.id.zoom_out);
		zoomOutButton.setOnClickListener(new View.OnClickListener()
		{
			@Override public void onClick(View v)
			{
				mapFragment.zoomOut();
			}
		});

		singleLocationRequest = new SingleLocationRequest(getActivity());

		return view;
	}

	@Override public void onStart()
	{
		super.onStart();

		String name = LocationUtil.isNewLocationApi() ? MODE_CHANGED : PROVIDERS_CHANGED_ACTION;
		getContext().registerReceiver(locationAvailabilityReceiver, new IntentFilter(name));

		LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationRequestFinishedReceiver,
				new IntentFilter(LocationRequestFragment.ACTION_FINISHED));

		if(ContextCompat.checkSelfPermission(getActivity(),
				Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
		{
			updateLocationAvailability();
		}
	}

	@Override public void onStop()
	{
		super.onStop();

		getContext().unregisterReceiver(locationAvailabilityReceiver);

		LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationRequestFinishedReceiver,
				new IntentFilter(LocationRequestFragment.ACTION_FINISHED));
	}

	/* ------------------------ Calls from the MapFragment ------------------------ */

	public void setMapFragment(MapFragment mapFragment)
	{
		this.mapFragment = mapFragment;
	}

	public void onMapOrientation(float rotation, float tilt)
	{
		compassNeedle.setOrientation(rotation, tilt);
	}

	public void onMapReady()
	{
		trackingButton.setActivated(mapFragment.isFollowingPosition());
		trackingButton.setCompassMode(mapFragment.isCompassMode());
	}

	public boolean requestUnglueViewFromPosition()
	{
		trackingButton.startAnimation( AnimationUtils.loadAnimation(getContext(), R.anim.pinch));
		return false;
	}

	public boolean requestUnglueViewFromRotation()
	{
		trackingButton.startAnimation( AnimationUtils.loadAnimation(getContext(), R.anim.pinch));
		return false;
	}

	private void updateLocationAvailability()
	{
		if(LocationUtil.isLocationSettingsOn(getActivity()))
		{
			onLocationIsEnabled();
		}
		else
		{
			onLocationIsDisabled();
		}
	}


	private void onLocationIsEnabled()
	{
		trackingButton.setState(LocationState.SEARCHING);
		mapFragment.startPositionTracking();
		singleLocationRequest.startRequest(LocationRequest.PRIORITY_HIGH_ACCURACY,
				new SingleLocationRequest.Callback()
				{
					@Override public void onLocation(Location location)
					{
						trackingButton.setState(LocationState.UPDATING);
					}
				});
	}

	private void onLocationIsDisabled()
	{
		trackingButton.setState(LocationState.ALLOWED);
		mapFragment.stopPositionTracking();
		singleLocationRequest.stopRequest();
	}

	private void setIsFollowingPosition(boolean follow)
	{
		trackingButton.setActivated(follow);
		mapFragment.setIsFollowingPosition(follow);
		if(!follow) setIsCompassMode(false);
	}

	private void setIsCompassMode(boolean compassMode)
	{
		trackingButton.setCompassMode(compassMode);
		mapFragment.setCompassMode(compassMode);
	}

	private void onLocationRequestFinished(LocationState state)
	{
		trackingButton.setState(state);
		if(state.isEnabled())
		{
			updateLocationAvailability();
		}
	}
}
