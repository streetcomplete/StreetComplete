package de.westnordost.streetcomplete.tangram;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.florent37.viewtooltip.ViewTooltip;
import com.mapzen.android.lost.api.LocationRequest;

import javax.inject.Inject;

import de.westnordost.streetcomplete.Injector;
import de.westnordost.streetcomplete.R;
import de.westnordost.streetcomplete.location.LocationRequestFragment;
import de.westnordost.streetcomplete.location.LocationState;
import de.westnordost.streetcomplete.location.LocationStateButton;
import de.westnordost.streetcomplete.location.LocationUtil;
import de.westnordost.streetcomplete.location.SingleLocationRequest;
import de.westnordost.streetcomplete.util.ViewUtils;

public class MapControlsFragment extends Fragment
{
	private static final String SHOW_CONTROLS = "ShowControls";

	private SingleLocationRequest singleLocationRequest;
	private MapFragment mapFragment;
	private ImageView compassNeedle;
	private LocationStateButton trackingButton;

	private ViewGroup leftSide, rightSide;
	private boolean isShowingControls = true;

	@Inject SharedPreferences prefs;

	private Listener listener;
	public interface Listener
	{
		void onClickCreateNote();
	}

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

	@Override public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Injector.instance.getApplicationComponent().inject(this);
		mapFragment = (MapFragment) getParentFragment();
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
									   Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_map_controls, container, false);
		compassNeedle = view.findViewById(R.id.compassNeedle);

		view.findViewById(R.id.compass).setOnClickListener(v ->
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
		});

		trackingButton = view.findViewById(R.id.gps_tracking);
		trackingButton.setOnClickListener(v ->
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
		});

		ImageButton zoomInButton = view.findViewById(R.id.zoom_in);
		zoomInButton.setOnClickListener(v -> mapFragment.zoomIn());
		ImageButton zoomOutButton = view.findViewById(R.id.zoom_out);
		zoomOutButton.setOnClickListener(v -> mapFragment.zoomOut());

		ImageButton createNoteButton = view.findViewById(R.id.create_note);
		createNoteButton.setOnClickListener(v ->
		{
			v.setEnabled(false);
			new Handler(Looper.getMainLooper()).postDelayed(() -> v.setEnabled(true), 200);
			listener.onClickCreateNote();
		});

		leftSide = view.findViewById(R.id.leftSide);
		rightSide = view.findViewById(R.id.rightSide);

		if(savedInstanceState != null)
		{
			isShowingControls = savedInstanceState.getBoolean(SHOW_CONTROLS);
		}

		singleLocationRequest = new SingleLocationRequest(getActivity());

		return view;
	}

	@Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		ViewUtils.postOnLayout(view, () ->
		{
			if(!isShowingControls)
			{
				hideAll(leftSide, -1);
				hideAll(rightSide, +1);
			}
		});


		mapFragment.onMapControlsCreated(this);
	}

	@Override public void onStart()
	{
		super.onStart();

		getContext().registerReceiver(locationAvailabilityReceiver, LocationUtil.createLocationAvailabilityIntentFilter());

		LocalBroadcastManager.getInstance(getContext()).registerReceiver(locationRequestFinishedReceiver,
				new IntentFilter(LocationRequestFragment.ACTION_FINISHED));

		updateLocationAvailability();
	}

	@Override public void onStop()
	{
		super.onStop();

		getContext().unregisterReceiver(locationAvailabilityReceiver);
		LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(locationRequestFinishedReceiver);
	}

	@Override public void onAttach(Context context)
	{
		super.onAttach(context);
		listener = (Listener) context;
	}

	@Override public void onSaveInstanceState(@NonNull Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putBoolean(SHOW_CONTROLS, isShowingControls);
	}

	/* ------------------------ Calls from the MapFragment ------------------------ */

	@AnyThread public void onMapOrientation(float rotation, float tilt)
	{
		getActivity().runOnUiThread(() -> {
			compassNeedle.setRotation((float) (180*rotation/Math.PI));
			compassNeedle.setRotationX((float) (180*tilt/Math.PI));
		});
	}

	public void onMapInitialized()
	{
		setTrackingButtonActivated(mapFragment.isFollowingPosition());
		trackingButton.setCompassMode(mapFragment.isCompassMode());
	}

	public boolean requestUnglueViewFromPosition()
	{
		return requestUnglueView();
	}

	public boolean requestUnglueViewFromRotation()
	{
		return requestUnglueView();
	}

	public void hideControls()
	{
		isShowingControls = false;
		animateAll(rightSide, +1, false, 120, 200);
		animateAll(leftSide, -1, false, 120, 200);
	}

	public void showControls()
	{
		isShowingControls = true;
		animateAll(rightSide, 0, true, 120, 200);
		animateAll(leftSide, 0, true, 120, 200);
	}

	private void hideAll(ViewGroup parent, int dir)
	{
		int w = parent.getWidth();
		for(int i = 0; i < parent.getChildCount(); ++i)
		{
			View v = parent.getChildAt(i);
			v.setTranslationX(w*dir);
		}
	}

	private void animateAll(ViewGroup parent, int dir, boolean in, int minDuration, int maxDuration)
	{
		int childCount = parent.getChildCount();
		int w = parent.getWidth();
		for(int i = 0; i < childCount; ++i)
		{
			View v = parent.getChildAt(i);

			int duration = minDuration + (maxDuration - minDuration) / Math.max(1, childCount-1) *
				(in ? childCount-1-i : i );
			ViewPropertyAnimator animator = v.animate().translationX(w*dir);
			animator.setDuration(duration);
			animator.setInterpolator(dir != 0 ? new AccelerateInterpolator() : new DecelerateInterpolator());
		}
	}

	private boolean requestUnglueView()
	{
		if(!LocationUtil.isLocationOn(getActivity()))
		{
			setIsFollowingPosition(false);
			return true;
		}

		trackingButton.startAnimation( AnimationUtils.loadAnimation(getContext(), R.anim.pinch));
		return false;
	}

	private void updateLocationAvailability()
	{
		if(LocationUtil.isLocationOn(getActivity()))
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
		singleLocationRequest.startRequest(LocationRequest.PRIORITY_HIGH_ACCURACY, location ->
		{
			if (getActivity() == null) return;
			trackingButton.setState(LocationState.UPDATING);
			showUnglueHint();
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
		setTrackingButtonActivated(follow);
		mapFragment.setIsFollowingPosition(follow);
		if(!follow) setIsCompassMode(false);
	}

	private void setTrackingButtonActivated(boolean activated)
	{
		trackingButton.setActivated(activated);
		showUnglueHint();
	}

	private void showUnglueHint()
	{
		//int timesShown = prefs.getInt(Prefs.UNGLUE_HINT_TIMES_SHOWN, 0);
		if(trackingButton.isActivated() && LocationUtil.isLocationOn(getActivity()))
		{
			ViewTooltip.on(trackingButton)
					.position(ViewTooltip.Position.LEFT)
					.text(getResources().getString(R.string.unglue_hint))
					.color(getResources().getColor(R.color.tooltip))
					.duration(3000)
					.show();
			//prefs.edit().putInt(Prefs.UNGLUE_HINT_TIMES_SHOWN, timesShown + 1).apply();
		}
	}

	private void setIsCompassMode(boolean compassMode)
	{
		trackingButton.setCompassMode(compassMode);
		mapFragment.setCompassMode(compassMode);
	}

	private void onLocationRequestFinished(LocationState state)
	{
		if(getActivity() == null) return;

		trackingButton.setState(state);
		if(state.isEnabled())
		{
			updateLocationAvailability();
		}
	}
}
